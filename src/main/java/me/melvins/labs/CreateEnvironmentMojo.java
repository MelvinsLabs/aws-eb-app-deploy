package me.melvins.labs;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest;
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
import java.util.List;


/**
 * Created by Melvin_Mathai on 10/1/2016.
 */
@Mojo(name = "CreateEnvironment", defaultPhase = LifecyclePhase.DEPLOY)
public class CreateEnvironmentMojo extends AbstractMojo {

    private static final Logger LOGGER =
            LogManager.getLogger(CreateEnvironmentMojo.class, new MessageFormatMessageFactory());

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

        CreateEnvironmentRequest createEnvironmentRequest = new CreateEnvironmentRequest();
        createEnvironmentRequest.setApplicationName(applicationName);
        createEnvironmentRequest.setVersionLabel(versionLabel);
        createEnvironmentRequest.setEnvironmentName(environmentName);
        createEnvironmentRequest.setCNAMEPrefix(cnamePrefix);
        createEnvironmentRequest.setGroupName(groupName);
        createEnvironmentRequest.setSolutionStackName(solutionStackName);
        createEnvironmentRequest.setOptionSettings(createOptionSettings());

        awsElasticBeanstalkClient.createEnvironment(createEnvironmentRequest);
    }

    private List<ConfigurationOptionSetting> createOptionSettings() {

        List<ConfigurationOptionSetting> configurationOptionSettingList = new ArrayList<ConfigurationOptionSetting>();

        if (optionSettings != null && optionSettings.trim().length() == 0) {
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

}
