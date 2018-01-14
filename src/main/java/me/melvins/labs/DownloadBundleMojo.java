package me.melvins.labs;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * @author Melvins
 */
@Mojo(name = "DownloadBundle", defaultPhase = LifecyclePhase.DEPLOY)
public class DownloadBundleMojo extends AbstractMojo {

    private static final Logger LOGGER =
            LogManager.getLogger(DownloadBundleMojo.class, new MessageFormatMessageFactory());

    @Parameter(required = true)
    private String s3Bucket;

    @Parameter(required = true)
    private String s3Key;

    @Parameter(required = true)
    private String file;

    @Override
    public String toString() {
        return "DownloadBundleMojo{" +
                "s3Bucket='" + s3Bucket + '\'' +
                ", s3Key='" + s3Key + '\'' +
                ", file='" + file + '\'' +
                '}';
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        LOGGER.info("Executing {0}", toString());

        AmazonS3Client amazonS3Client =
                new AmazonS3Client(InstanceProfileCredentialsProvider.getInstance())
                        .withRegion(Regions.US_WEST_2);

        GetObjectRequest getObjectRequest = new GetObjectRequest(s3Bucket, s3Key);

        try {
            try (S3Object s3Object = amazonS3Client.getObject(getObjectRequest)) {
                try (S3ObjectInputStream s3is = s3Object.getObjectContent()) {

                    FileOutputStream fos = new FileOutputStream(new File(file));
                    byte[] read_buf = new byte[1024];
                    int read_len = 0;
                    while ((read_len = s3is.read(read_buf)) > 0) {
                        fos.write(read_buf, 0, read_len);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }

    }

}
