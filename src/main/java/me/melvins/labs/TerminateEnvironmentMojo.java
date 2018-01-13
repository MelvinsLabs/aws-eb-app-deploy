/*
 *
 */

package me.melvins.labs;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest;
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
@Mojo(name = "TerminateEnvironment", defaultPhase = LifecyclePhase.DEPLOY)
public class TerminateEnvironmentMojo extends AbstractMojo {

    private static final Logger LOGGER =
            LogManager.getLogger(TerminateEnvironmentMojo.class, new MessageFormatMessageFactory());

    @Parameter(required = true)
    private String environmentId;

    @Override
    public String toString() {
        return "TerminateEnvironmentMojo{" +
                "environmentId='" + environmentId + '\'' +
                '}';
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        LOGGER.info("Executing {0}", toString());

        AWSElasticBeanstalkClient awsElasticBeanstalkClient =
                new AWSElasticBeanstalkClient(new ProfileCredentialsProvider())
                        .withRegion(Regions.US_WEST_2);

        TerminateEnvironmentRequest terminateEnvironmentRequest = new TerminateEnvironmentRequest();
        terminateEnvironmentRequest.setEnvironmentId(environmentId);

        awsElasticBeanstalkClient.terminateEnvironment(terminateEnvironmentRequest);
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

}
