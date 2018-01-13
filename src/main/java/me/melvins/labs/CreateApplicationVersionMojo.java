/*
 *
 */

package me.melvins.labs;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.S3Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


/**
 * @author Melvins
 */
@Mojo(name = "CreateApplicationVersion", defaultPhase = LifecyclePhase.DEPLOY)
public class CreateApplicationVersionMojo extends AbstractMojo {

    private static final Logger LOGGER =
            LogManager.getLogger(CreateApplicationVersionMojo.class, new MessageFormatMessageFactory());

    @Parameter(required = true)
    private String applicationName;

    @Parameter(required = true)
    private String versionLabel;

    @Parameter(required = true)
    private String s3Bucket;

    @Parameter(required = true)
    private String s3Key;

    @Override
    public String toString() {
        return "CreateApplicationVersionMojo{" +
                "applicationName='" + applicationName + '\'' +
                ", versionLabel='" + versionLabel + '\'' +
                ", s3Bucket='" + s3Bucket + '\'' +
                ", s3Key='" + s3Key + '\'' +
                '}';
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        LOGGER.info("Executing {0}", toString());

        AWSElasticBeanstalkClient awsElasticBeanstalkClient =
                new AWSElasticBeanstalkClient(new ProfileCredentialsProvider())
                        .withRegion(Regions.US_WEST_2);

        CreateApplicationVersionRequest createApplicationVersionRequest = new CreateApplicationVersionRequest();
        createApplicationVersionRequest.setApplicationName(applicationName);
        createApplicationVersionRequest.setVersionLabel(versionLabel);
        createApplicationVersionRequest.setSourceBundle(new S3Location().withS3Bucket(s3Bucket).withS3Key(s3Key));

        try {
            awsElasticBeanstalkClient.createApplicationVersion(createApplicationVersionRequest);

        } catch (Exception ex) {
            LOGGER.error("Application Version Already Exist");
        }
    }

}
