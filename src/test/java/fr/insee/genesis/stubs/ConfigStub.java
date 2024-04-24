package fr.insee.genesis.stubs;

import cucumber.TestConstants;
import fr.insee.genesis.configuration.Config;

public class ConfigStub extends Config {

	private static final String DATA_SOURCE = TestConstants.TEST_RESOURCES_DIRECTORY;

	private static final String SPEC_SOURCE = TestConstants.TEST_RESOURCES_DIRECTORY;



	@Override
	public String getDataFolderSource() {
		return DATA_SOURCE;
	}

	@Override
	public String getSpecFolderSource() {
		return SPEC_SOURCE;
	}


}
