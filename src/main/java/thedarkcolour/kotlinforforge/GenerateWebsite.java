package thedarkcolour.kotlinforforge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GenerateWebsite {
    public static void main(String[] args) {
        var f = new File("thedarkcolour/kotlinforforge/");

        assert f.isDirectory();

        try {
            for (var file : f.listFiles()) {
                if (file.isDirectory()) {
                    for (var jar : file.listFiles()) {
                        if (jar.isFile() && (jar.getName().endsWith(".jar") || jar.getName().endsWith(".pom"))) {
                            var sum = writeSum(jar, "MD5", ".md5");
                            String path = sum.getPath();
                        }
                    }
                }
            }

            var metadata = new File("thedarkcolour/kotlinforforge/maven-metadata.xml");
            writeSum(metadata, "MD5", ".md5");
            writeSum(metadata, "SHA-1", ".sha1");
        } catch (IOException | NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }
    }


    public static File writeSum(File file, String digest, String extension) throws IOException, NoSuchAlgorithmException {
        var sumFile = new File(file.getPath() + extension);
        var sum = new BigInteger(1, MessageDigest.getInstance(digest).digest(Files.readAllBytes(file.toPath()))).toString(16);
        sumFile.setWritable(true);

        try (var writer = new FileWriter(sumFile)) {
            try (var bufferedWriter = new BufferedWriter(writer)) {
                bufferedWriter.write(sum);
                sumFile.createNewFile();
            }
        }

        return sumFile;
    }
}
