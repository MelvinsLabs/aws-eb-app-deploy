/*
 *
 */

package me.melvins.labs;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Arrays;

import static me.melvins.labs.CreateEnvironmentMojo.HEALTH_GREEN;
import static me.melvins.labs.CreateEnvironmentMojo.findCName;
import static me.melvins.labs.CreateEnvironmentMojo.findEnvName;


/**
 * @author Melvins
 */
@Mojo(name = "ReplaceEnvironment", defaultPhase = LifecyclePhase.DEPLOY)
public class ReplaceEnvironmentMojo extends AbstractMojo {

    private static final Logger LOGGER =
            LogManager.getLogger(ReplaceEnvironmentMojo.class, new MessageFormatMessageFactory());

    @Parameter(required = true)
    private String applicationName;

    @Parameter(required = true)
    private String versionLabel;

    @Parameter(required = true)
    private String environmentName;

    @Parameter(required = true)
    private String cnamePrefix;

    @Parameter(required = false)
    private String groupName;

    @Parameter(required = true)
    private String solutionStackName;

    @Parameter(required = false)
    private String optionSettings;

    @Parameter
    private String optionSettingsFileName;

    private String cName;

    private String envName;

    @Override
    public String toString() {
        return "ReplaceEnvironmentMojo{" +
                "applicationName='" + applicationName + '\'' +
                ", versionLabel='" + versionLabel + '\'' +
                ", environmentName='" + environmentName + '\'' +
                ", cnamePrefix='" + cnamePrefix + '\'' +
                ", groupName='" + groupName + '\'' +
                ", solutionStackName='" + solutionStackName + '\'' +
                ", optionSettings='" + optionSettings + '\'' +
                ", optionSettingsFileName='" + optionSettingsFileName + '\'' +
                ", cName='" + cName + '\'' +
                ", envName='" + envName + '\'' +
                '}';
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        LOGGER.info("Executing {0}", toString());

        AWSElasticBeanstalkClient awsElasticBeanstalkClient =
                new AWSElasticBeanstalkClient(InstanceProfileCredentialsProvider.getInstance())
                        .withRegion(Regions.US_WEST_2);

        cName = findCName(awsElasticBeanstalkClient, cnamePrefix);

        envName = findEnvName(awsElasticBeanstalkClient, applicationName, environmentName);

        initiateCreateEnvironment();

        DescribeEnvironmentsRequest describeEnvironmentsRequest = new DescribeEnvironmentsRequest();
        describeEnvironmentsRequest.setEnvironmentNames(Arrays.asList(envName));

        DescribeEnvironmentsResult describeEnvironmentsResult =
                awsElasticBeanstalkClient.describeEnvironments(describeEnvironmentsRequest);

        String health = describeEnvironmentsResult.getEnvironments().get(0).getHealth();

        if (HEALTH_GREEN.equalsIgnoreCase(health)) {

            if (cName.contains("-0")) {
                // Initiate Swap
                describeEnvironmentsRequest = new DescribeEnvironmentsRequest();
                describeEnvironmentsRequest.setApplicationName(applicationName);
                describeEnvironmentsRequest.setEnvironmentNames(
                        Arrays.asList(environmentName + "-B", environmentName + "-G"));

                describeEnvironmentsResult =
                        awsElasticBeanstalkClient.describeEnvironments(describeEnvironmentsRequest);

                String newEnvId = null;
                String oldEnvId = null;
                for (EnvironmentDescription env : describeEnvironmentsResult.getEnvironments()) {
                    if (env.getCNAME().endsWith("-0")) {
                        newEnvId = env.getEnvironmentId();

                    } else {
                        oldEnvId = env.getEnvironmentId();
                    }
                }

                initiateSwapEnvironment(newEnvId, oldEnvId);

                initiateTerminateEnvironment(oldEnvId);

            } else {
                LOGGER.info("No Need To Swap As The New Environment Is The Only One");
            }

        } else {
            LOGGER.error("Unable To Get A Health Env After Wait [{0}]", health);
        }
    }

    private void initiateCreateEnvironment()
            throws MojoExecutionException, MojoFailureException {

        CreateEnvironmentMojo createEnvironmentMojo = new CreateEnvironmentMojo();
        createEnvironmentMojo.setApplicationName(applicationName);
        createEnvironmentMojo.setVersionLabel(versionLabel);
        createEnvironmentMojo.setEnvName(envName);
        createEnvironmentMojo.setcName(cName);
        createEnvironmentMojo.setGroupName(groupName);
        createEnvironmentMojo.setSolutionStackName(solutionStackName);
        createEnvironmentMojo.setOptionSettings(optionSettings);
        createEnvironmentMojo.setOptionSettings(optionSettingsFileName);

        createEnvironmentMojo.execute();
    }

    private void initiateSwapEnvironment(String anEnvId,
                                         String anotherEnvId)
            throws MojoExecutionException, MojoFailureException {

        SwapEnvironmentMojo swapEnvironmentMojo = new SwapEnvironmentMojo();
        swapEnvironmentMojo.setSourceEnvironmentId(anEnvId);
        swapEnvironmentMojo.setDestinationEnvironmentId(anotherEnvId);

        swapEnvironmentMojo.execute();
    }

    private void initiateTerminateEnvironment(String oldEnvId)
            throws MojoFailureException, MojoExecutionException {

        TerminateEnvironmentMojo terminateEnvironmentMojo = new TerminateEnvironmentMojo();
        terminateEnvironmentMojo.setEnvironmentId(oldEnvId);

        terminateEnvironmentMojo.execute();
    }

}
