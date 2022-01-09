package net.midnightmc.core.world;

import lombok.Getter;
import net.midnightmc.core.utils.FaweUtil;
import net.midnightmc.core.utils.MessageUtil;
import net.midnightmc.core.utils.S3Util;
import net.midnightmc.core.utils.ScheduleUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.util.List;

/**
 * 맵 불러오기/저장/목록보기 등을 하는 클래스
 */
public final class MapAPI {

    @Getter
    private static final MapAPI instance = new MapAPI();

    private MapAPI() {}

    /**
     * 맵 목록 조회
     * Async 추천
     *
     * @return 맵 목록
     */
    public @NotNull List<String> listMaps() {
        try {
            ListObjectsRequest request = ListObjectsRequest
                    .builder()
                    .bucket(S3Util.S3_BUCKET)
                    .build();
            S3Client s3 = S3Util.getS3Client();
            ListObjectsResponse res = s3.listObjects(request);
            List<S3Object> objects = res.contents();
            return objects.stream().map(S3Object::key).filter(s -> !s.endsWith("/"))
                    .filter(s -> s.startsWith("maps/")).map(s -> s.substring(5)).toList();
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return List.of();
    }

    /**
     * 맵 로드
     * Async 추천
     *
     * @param name  맵 이름
     * @param world 맵을 로드할 월드
     * @return 맵 크기. 맵 로드에 실패할 경우 0.
     */
    public boolean loadMap(@NotNull final String name, @NotNull final World world, final boolean air) {
        File file = new File(world.getWorldFolder(), "cache.schematic");
        if (!S3Util.download(file, "maps/" + name)) {
            return false;
        }
        int size = FaweUtil.paste(world, file, air);
        if (size == 0) {
            return false;
        }
        FileUtils.deleteQuietly(file);
        ScheduleUtil.callSync(() -> {
            world.getWorldBorder().setCenter(0.0, 0.0);
            world.getWorldBorder().setSize(size);
            return null;
        });
        Bukkit.getConsoleSender().sendMessage(MessageUtil.getComponent("&7" + name + " &a로드 성공"));
        return true;
    }

    /**
     * 맵 저장
     * Async 추천
     *
     * @param name  맵 이름
     * @param world 저장할 맵이 있는 월드
     */
    public boolean saveMap(final String name, final World world) {
        File file = new File(world.getWorldFolder(), "cache.schematic");
        if (!FaweUtil.save(world, file)) {
            return false;
        }
        if (!S3Util.upload(file, "maps/" + name)) {
            return false;
        }
        FileUtils.deleteQuietly(file);
        return true;
    }

}
