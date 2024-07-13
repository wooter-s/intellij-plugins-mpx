package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.bc.JpsFlexModuleOrProjectCompilerOptions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementCollection;
import org.jetbrains.jps.model.ex.JpsCompositeElementBase;

import java.util.ArrayList;
import java.util.List;

public class JpsFlexBuildConfigurationManagerImpl extends JpsCompositeElementBase<JpsFlexBuildConfigurationManagerImpl>
  implements JpsFlexBuildConfigurationManager {

  private static final Logger LOG = Logger.getInstance(JpsFlexBuildConfigurationManagerImpl.class.getName());

  private JpsFlexBuildConfiguration myActiveConfiguration;

  public JpsFlexBuildConfigurationManagerImpl() {
    myContainer.setChild(JpsFlexBuildConfigurationImpl.COLLECTION_ROLE);
    myContainer.setChild(JpsFlexCompilerOptionsRole.INSTANCE);
  }

  private JpsFlexBuildConfigurationManagerImpl(final JpsFlexBuildConfigurationManagerImpl original) {
    super(original);
  }

  @Override
  @NotNull
  public JpsFlexBuildConfigurationManagerImpl createCopy() {
    return new JpsFlexBuildConfigurationManagerImpl(this);
  }

// ------------------------------

  @Override
  public List<JpsFlexBuildConfiguration> getBuildConfigurations() {
    return myContainer.getChild(JpsFlexBuildConfigurationImpl.COLLECTION_ROLE).getElements();
  }

  //public JpsFlexBuildConfiguration addBuildConfiguration(String name) {
  //  return myContainer.getChild(JpsFlexBuildConfigurationImpl.COLLECTION_ROLE).addChild(new JpsFlexBuildConfigurationImpl(name));
  //}

  @Override
  public JpsFlexBuildConfiguration getActiveConfiguration() {
    return myActiveConfiguration;
  }

  @Override
  @Nullable
  public JpsFlexBuildConfiguration findConfigurationByName(final String name) {
    for (JpsFlexBuildConfiguration configuration : getBuildConfigurations()) {
      if (configuration.getName().equals(name)) {
        return configuration;
      }
    }

    return null;
  }

  @Override
  public JpsFlexModuleOrProjectCompilerOptions getModuleLevelCompilerOptions() {
    return myContainer.getChild(JpsFlexCompilerOptionsRole.INSTANCE);
  }

  @Override
  public JpsFlexBuildConfiguration createCopy(@NotNull final JpsFlexBuildConfiguration bc) {
    final JpsFlexBuildConfigurationImpl copy = ((JpsFlexBuildConfigurationImpl)bc).createConfigurationCopy();
    copy.setParent(((JpsFlexBuildConfigurationImpl)bc).getParent());
    return copy;
  }

  @Override
  public JpsFlexBuildConfiguration createTemporaryCopyForCompilation(@NotNull final JpsFlexBuildConfiguration bc) {
    final JpsFlexBuildConfigurationImpl copy = ((JpsFlexBuildConfigurationImpl)bc).createConfigurationCopy();
    copy.setParent(((JpsFlexBuildConfigurationImpl)bc).getParent());
    copy.setTempBCForCompilation(true);
    return copy;
  }

  private void updateActiveConfiguration(@Nullable final String activeBCName) {
    final List<JpsFlexBuildConfiguration> bcs = getBuildConfigurations();
    if (bcs.isEmpty()) {
      LOG.error("No Flex build configurations");
      myActiveConfiguration = null;
    }
    else {
      myActiveConfiguration =
        activeBCName != null ? ContainerUtil.find(bcs, bc -> bc.getName().equals(activeBCName)) : null;

      if (myActiveConfiguration == null) {
        myActiveConfiguration = bcs.get(0);
      }
    }
  }

// ------------------------------

  public State getState() {
    final State state = new State();
    for (JpsFlexBuildConfiguration configuration : getBuildConfigurations()) {
      state.CONFIGURATIONS.add(((JpsFlexBuildConfigurationImpl)configuration).getState());
    }

    state.ACTIVE_BC_NAME = myActiveConfiguration != null ? myActiveConfiguration.getName() : null;

    state.MODULE_LEVEL_COMPILER_OPTIONS = ((JpsFlexCompilerOptionsImpl)getModuleLevelCompilerOptions()).getState();
    return state;
  }

  public void loadState(final State state) {
    //if (myModule == null) {
    //  throw new IllegalStateException("Cannot load state of a dummy config manager instance");
    //}
    final JpsElementCollection<JpsFlexBuildConfiguration> bcs = myContainer.getChild(JpsFlexBuildConfigurationImpl.COLLECTION_ROLE);
    LOG.assertTrue(bcs.getElements().size() == 0);

    for (JpsFlexBCState configurationState : state.CONFIGURATIONS) {
      JpsFlexBuildConfigurationImpl bc = new JpsFlexBuildConfigurationImpl(configurationState.NAME);
      bc.loadState(configurationState);
      bcs.addChild(bc);
    }

    if (bcs.getElements().isEmpty()) {
      LOG.warn("Flex build configurations not loaded from *.iml.");
      bcs.addChild(new JpsFlexBuildConfigurationImpl(JpsFlexBuildConfiguration.UNNAMED));
    }

    updateActiveConfiguration(state.ACTIVE_BC_NAME);

    ((JpsFlexCompilerOptionsImpl)getModuleLevelCompilerOptions()).loadState(state.MODULE_LEVEL_COMPILER_OPTIONS);
  }

  public static class State {
    @XCollection(propertyElementName = "configurations", elementName = "configuration")
    public List<JpsFlexBCState> CONFIGURATIONS = new ArrayList<>();

    @Property(surroundWithTag = false)
    public JpsFlexCompilerOptionsImpl.State MODULE_LEVEL_COMPILER_OPTIONS = new JpsFlexCompilerOptionsImpl.State();

    @Attribute("active")
    public String ACTIVE_BC_NAME;
  }
}
