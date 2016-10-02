package me.melvins.labs;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest;
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
 * Created by Melvin_Mathai on 10/1/2016.
 */
@Mojo(name = "CreateApplication", defaultPhase = LifecyclePhase.DEPLOY)
public class CreateApplicationMojo extends AbstractMojo {

    private static final Logger LOGGER =
            LogManager.getLogger(CreateApplicationMojo.class, new MessageFormatMessageFactory());

    @Parameter(required = true)
    private String applicationName;

    @Override
    public String toString() {
        return "CreateApplicationMojo{" +
                "applicationName='" + applicationName + '\'' +
                '}';
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        LOGGER.info("Executing {0}", toString());

        AWSElasticBeanstalkClient awsElasticBeanstalkClient =
                new AWSElasticBeanstalkClient(new ProfileCredentialsProvider())
                        .withRegion(Regions.US_WEST_2);

        CreateApplicationRequest createApplicationRequest = new CreateApplicationRequest();
        createApplicationRequest.setApplicationName(applicationName);

        try {
            awsElasticBeanstalkClient.createApplication(createApplicationRequest);

        } catch (Exception ex) {
            LOGGER.warn("Application Already Exist");
        }
    }

}
