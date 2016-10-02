package me.melvins.labs;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest;
import com.amazonaws.services.elasticbeanstalk.model.SwapEnvironmentCNAMEsRequest;
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
@Mojo(name = "SwapEnvironment", defaultPhase = LifecyclePhase.DEPLOY)
public class SwapEnvironmentMojo extends AbstractMojo {

    private static final Logger LOGGER =
            LogManager.getLogger(SwapEnvironmentMojo.class, new MessageFormatMessageFactory());

    @Parameter(required = true)
    private String sourceEnvironmentId;

    @Parameter(required = true)
    private String destinationEnvironmentId;

    @Override
    public String toString() {
        return "SwapEnvironmentMojo{" +
                "sourceEnvironmentId='" + sourceEnvironmentId + '\'' +
                ", destinationEnvironmentId='" + destinationEnvironmentId + '\'' +
                '}';
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        LOGGER.info("Executing {0}", toString());

        AWSElasticBeanstalkClient awsElasticBeanstalkClient =
                new AWSElasticBeanstalkClient(new ProfileCredentialsProvider())
                        .withRegion(Regions.US_WEST_2);

        SwapEnvironmentCNAMEsRequest swapEnvironmentCNAMEsRequest = new SwapEnvironmentCNAMEsRequest();
        swapEnvironmentCNAMEsRequest.setSourceEnvironmentId(sourceEnvironmentId);
        swapEnvironmentCNAMEsRequest.setDestinationEnvironmentId(destinationEnvironmentId);

        awsElasticBeanstalkClient.swapEnvironmentCNAMEs(swapEnvironmentCNAMEsRequest);
    }

}
