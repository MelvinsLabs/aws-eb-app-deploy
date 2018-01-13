/*
 *
 */

package me.melvins.labs;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityRequest;
import com.amazonaws.services.elasticbeanstalk.model.CheckDNSAvailabilityResult;
import com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentHealthResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import me.melvins.labs.utils.TimeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFormatMessageFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Melvins
 */
@Mojo(name = "CreateEnvironment", defaultPhase = LifecyclePhase.DEPLOY)
public class CreateEnvironmentMojo extends AbstractMojo {

    private static final Logger LOGGER =
            LogManager.getLogger(CreateEnvironmentMojo.class, new MessageFormatMessageFactory());

    public static final List<String> HEALTH_STATUS_GREEN = Arrays.asList("OK", "Info");

    private static final int MAX_WAIT_COUNT = 6;

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

    @Override
    public String toString() {
        return "CreateEnvironmentMojo{" +
                "applicationName='" + applicationName + '\'' +
                ", versionLabel='" + versionLabel + '\'' +
                ", environmentName='" + environmentName + '\'' +
                ", cnamePrefix='" + cnamePrefix + '\'' +
                ", groupName='" + groupName + '\'' +
                ", solutionStackName='" + solutionStackName + '\'' +
                ", optionSettings='" + optionSettings + '\'' +
                '}';
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        LOGGER.info("Executing {0}", toString());

        AWSElasticBeanstalkClient awsElasticBeanstalkClient =
                new AWSElasticBeanstalkClient(new ProfileCredentialsProvider())
                        .withRegion(Regions.US_WEST_2);

        // Check cName Availability.
        CheckDNSAvailabilityRequest checkDNSAvailabilityRequest = new CheckDNSAvailabilityRequest();
        checkDNSAvailabilityRequest.setCNAMEPrefix(cnamePrefix);

        CheckDNSAvailabilityResult checkDNSAvailabilityResult =
                awsElasticBeanstalkClient.checkDNSAvailability(checkDNSAvailabilityRequest);

        String cName;
        if (checkDNSAvailabilityResult.getAvailable().booleanValue()) {
            // If cName Available, proceed as there is no existing environment.
            cName = cnamePrefix;

        } else {
            // If cName not Available, suffix cName, as there is an existing environment.
            // Assuming environment belongs to the same App.
            cName = cnamePrefix + "-0";
        }

        DescribeEnvironmentsRequest describeEnvironmentsRequest = new DescribeEnvironmentsRequest();
        describeEnvironmentsRequest.setApplicationName(applicationName);
        describeEnvironmentsRequest.setEnvironmentNames(Arrays.asList(environmentName + "-B"));

        DescribeEnvironmentsResult describeEnvironmentsResult =
                awsElasticBeanstalkClient.describeEnvironments(describeEnvironmentsRequest);

        String blueGreenSuffix = "-B";
        if (describeEnvironmentsResult.getEnvironments().size() > 0) {
            blueGreenSuffix = "-G";
        }
        // else Blue Environment Do Not Exist.

        CreateEnvironmentRequest createEnvironmentRequest = new CreateEnvironmentRequest();
        createEnvironmentRequest.setApplicationName(applicationName);
        createEnvironmentRequest.setVersionLabel(versionLabel);
        createEnvironmentRequest.setEnvironmentName(environmentName + blueGreenSuffix);
        createEnvironmentRequest.setCNAMEPrefix(cName);
        createEnvironmentRequest.setGroupName(groupName);
        createEnvironmentRequest.setSolutionStackName(solutionStackName);
        createEnvironmentRequest.setOptionSettings(createOptionSettings());

        CreateEnvironmentResult createEnvironmentResult =
                awsElasticBeanstalkClient.createEnvironment(createEnvironmentRequest);

        verifyEnvironmentHealth(awsElasticBeanstalkClient, createEnvironmentResult);
    }

    private void verifyEnvironmentHealth(AWSElasticBeanstalkClient awsElasticBeanstalkClient,
                                         CreateEnvironmentResult createEnvironmentResult) {

        String newEnvironmentId = createEnvironmentResult.getEnvironmentId();
        String healthStatus = createEnvironmentResult.getHealthStatus();

        int waitCount = 0;
        while (waitCount < MAX_WAIT_COUNT) {

            if (HEALTH_STATUS_GREEN.contains(healthStatus)) {
                LOGGER.info("Health Status Of Env [{0}] Is {1}", newEnvironmentId, healthStatus);

            } else {
                waitCount++;

                LOGGER.info("Env [{0}] Health Status [{1}] Waiting {2}/{3}",
                        newEnvironmentId, healthStatus, waitCount, MAX_WAIT_COUNT);

                TimeUtils.sleeper(1000 * 60 * 5);

                DescribeEnvironmentHealthRequest describeEnvironmentHealthRequest = new
                        DescribeEnvironmentHealthRequest();
                describeEnvironmentHealthRequest.setEnvironmentId(newEnvironmentId);

                DescribeEnvironmentHealthResult describeEnvironmentHealthResult =
                        awsElasticBeanstalkClient.describeEnvironmentHealth(describeEnvironmentHealthRequest);

                healthStatus = describeEnvironmentHealthResult.getHealthStatus();
            }
        }
    }

    private List<ConfigurationOptionSetting> createOptionSettings() {

        List<ConfigurationOptionSetting> configurationOptionSettingList = new ArrayList<ConfigurationOptionSetting>();

        LOGGER.info(optionSettings);
        if (optionSettings != null && optionSettings.trim().length() != 0) {
            LOGGER.info(optionSettings);
            String[] allOptionSettings = optionSettings.split(" ");
            for (String eachOptionSetting : allOptionSettings) {
                String[] optionSettingComponents = eachOptionSetting.split("=");

                LOGGER.info(optionSettingComponents);
                ConfigurationOptionSetting configurationOptionSetting = new ConfigurationOptionSetting();
                configurationOptionSetting.setNamespace(optionSettingComponents[0]);
                configurationOptionSetting.setOptionName(optionSettingComponents[1]);
                configurationOptionSetting.setValue(optionSettingComponents[2]);

                configurationOptionSettingList.add(configurationOptionSetting);
            }
        }

        return configurationOptionSettingList;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public void setCnamePrefix(String cnamePrefix) {
        this.cnamePrefix = cnamePrefix;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setSolutionStackName(String solutionStackName) {
        this.solutionStackName = solutionStackName;
    }

    public void setOptionSettings(String optionSettings) {
        this.optionSettings = optionSettings;
    }
}
