package com.intellij.javascript.bower;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.intellij.openapi.util.NullableLazyValue.lazyNullable;

public final class BowerCommandLineUtil {
  private static final Logger LOG = Logger.getInstance(BowerCommandLineUtil.class);
  private static final NullableLazyValue<File> TEMP_DIR = lazyNullable(() -> {
    try {
      return FileUtil.createTempDirectory("jetbrains-intellij-bower", null);
    }
    catch (IOException e) {
      LOG.warn("Cannot create temp dir", e);
      return null;
    }
  });

  private BowerCommandLineUtil() {
  }

  @NotNull
  public static ProcessOutput runBowerCommand(@Nullable ProgressIndicator indicator,
                                              @NotNull BowerSettings settings,
                                              String... commands) throws ExecutionException {
    BowerCommandRun bowerCommandRun = startBowerCommand(settings, commands);
    return bowerCommandRun.captureOutput(indicator, TimeUnit.MINUTES.toMillis(10));
  }

  @NotNull
  public static BowerCommandRun startBowerCommand(@NotNull BowerSettings settings, String... commands) throws ExecutionException {
    GeneralCommandLine commandLine = createCommandLine(settings);
    commandLine.addParameters(commands);
    LOG.info("Running bower command: " + commandLine.getCommandLineString());
    return new BowerCommandRun(new KillableColoredProcessHandler(commandLine));
  }

  @NotNull
  public static GeneralCommandLine createCommandLine(@NotNull BowerSettings settings) throws ExecutionException {
    NodeJsInterpreter interpreter = settings.getInterpreter();
    NodeJsLocalInterpreter localInterpreter = NodeJsLocalInterpreter.castAndValidate(interpreter);
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setCharset(StandardCharsets.UTF_8);
    commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE);
    File workingDir = getWorkingDir(settings);
    if (workingDir != null) {
      commandLine.setWorkDirectory(workingDir);
    }
    else {
      LOG.info("Working directory not specified");
    }
    File mainBowerJsFile = getMainBowerJsFile(settings.getBowerPackage());
    commandLine.setExePath(localInterpreter.getInterpreterSystemDependentPath());
    commandLine.addParameter(mainBowerJsFile.getAbsolutePath());
    return commandLine;
  }

  @NotNull
  private static File getMainBowerJsFile(@NotNull NodePackage bowerPackage) throws ExecutionException {
    String errorMessage = bowerPackage.getErrorMessage(BowerSettingsManager.BOWER_PACKAGE_NAME);
    if (errorMessage != null) {
      throw new ExecutionException(errorMessage);
    }
    String path = "bin" + File.separator + "bower";
    File file = new File(bowerPackage.getSystemDependentPath(), path);
    if (!file.isFile()) {
      throw new ExecutionException(BowerBundle.message("bower.dialog.message.specify.package", path));
    }
    return file;
  }

  @Nullable
  private static File getWorkingDir(@NotNull BowerSettings settings) {
    File bowerConfigFile = new File(settings.getBowerJsonPath());
    if (bowerConfigFile.isFile()) {
      return bowerConfigFile.getParentFile();
    }
    return TEMP_DIR.getValue();
  }
}
