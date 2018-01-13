package me.melvins.labs;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
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


/**
 * @author Melvins
 */
@Mojo(name = "UploadBundle", defaultPhase = LifecyclePhase.DEPLOY)
public class UploadBundleMojo extends AbstractMojo {

    private static final Logger LOGGER =
            LogManager.getLogger(UploadBundleMojo.class, new MessageFormatMessageFactory());

    @Parameter(required = true)
    private String s3Bucket;

    @Parameter(required = true)
    private String s3Key;

    @Parameter(required = true)
    private String file;

    @Override
    public String toString() {
        return "UploadBundleMojo{" +
                "s3Bucket='" + s3Bucket + '\'' +
                ", s3Key='" + s3Key + '\'' +
                ", file='" + file + '\'' +
                '}';
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        LOGGER.info("Executing {0}", toString());

        AmazonS3Client amazonS3Client =
                new AmazonS3Client(new InstanceProfileCredentialsProvider())
                        .withRegion(Regions.US_WEST_2);

        File file = new File(this.file);

        PutObjectRequest putObjectRequest = new PutObjectRequest(s3Bucket, s3Key, file);

        amazonS3Client.putObject(putObjectRequest);
    }

}
