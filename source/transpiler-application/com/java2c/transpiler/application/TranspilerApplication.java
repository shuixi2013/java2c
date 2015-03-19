package com.java2c.transpiler.application;

import com.java2c.javaCompiler.processors.CodeTreeUserAdaptingProcessor;
import com.java2c.transpiler.TranspilingCodeTreeUser;
import com.java2c.javaCompiler.*;
import com.java2c.javaCompiler.javaSourceFiles.JavaSourceFilesFinder;
import com.java2c.javaCompiler.pathExpressions.IllegalRelativePathException;
import com.java2c.javaCompiler.pathExpressions.IllegalRelativePathExpressionException;
import com.java2c.javaCompiler.pathExpressions.RelativePathExpression;
import com.java2c.javaCompiler.pathExpressions.RootPathAndExpression;
import com.java2c.transpiler.elementHandlers.RootElementHandler;
import com.java2c.transpiler.elementHandlers.PackageElementHandler;
import com.java2c.transpiler.elementHandlers.TypeElementHandler;
import com.java2c.transpiler.warnings.Warnings;
import com.java2c.utility.ImpossibleStateException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.tools.JavaCompiler;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static com.java2c.utility.EnglishFormatter.format;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.delete;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public final class TranspilerApplication
{
	@SuppressWarnings("NullableProblems")
	@NotNull
	private static final RelativePathExpression ModuleNamePathExpression;

	static
	{
		try
		{
			ModuleNamePathExpression = new RelativePathExpression("%m");
		}
		catch (final IllegalRelativePathExpressionException ignored)
		{
			throw new ImpossibleStateException();
		}
	}

	@NotNull
	@NonNls
	private static final String TemporaryFolderPrefix = "java2c-";

	@NotNull
	private final Warnings warnings;

	@NotNull
	private final List<ModuleName> moduleNames;

	@NotNull
	private final RootPathAndExpression moduleRoot;

	@NotNull
	private final RootPathAndExpression sourceOutput;

	@NotNull
	private final Collection<Path> additionalClassPath;

	@NotNull
	private final JavaModuleCompiler javaModuleCompiler;

	@NotNull
	private final CodeTreeUserAdaptingProcessor processor;

	@SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
	public TranspilerApplication(@NotNull final Warnings warnings, @NotNull final List<ModuleName> moduleNames, @NotNull final RootPathAndExpression moduleRoot, @NotNull final RootPathAndExpression sourceOutput, @NotNull final Collection<Path> additionalClassPath)
	{
		this.warnings = warnings;
		this.moduleNames = moduleNames;
		this.moduleRoot = moduleRoot;
		this.sourceOutput = sourceOutput;
		this.additionalClassPath = additionalClassPath;

		javaModuleCompiler = new JavaModuleCompiler(warnings, new WarningsAdaptingDiagnosticListener(warnings), getJavaCompiler(), new JavaSourceFilesFinder(warnings));

		processor = new CodeTreeUserAdaptingProcessor(new TranspilingCodeTreeUser(new RootElementHandler(new PackageElementHandler(), new TypeElementHandler())));
	}

	public void execute()
	{
		final Path classOutputRootPath = createTemporaryClassOutputPath();
		try
		{
			useClassOutputRootPath(classOutputRootPath);
		}
		catch (final IllegalRelativePathException | FatalCompilationException e)
		{
			warnings.fatal(e);
		}
		finally
		{
			try
			{
				delete(classOutputRootPath);
			}
			catch (final IOException ignored)
			{
			}
		}
	}

	private void useClassOutputRootPath(@NotNull final Path classOutputRootPath) throws IllegalRelativePathException, FatalCompilationException
	{
		final RootPathAndExpression classOutput = new RootPathAndExpression(classOutputRootPath, ModuleNamePathExpression);

		for (final ModuleName moduleName : moduleNames)
		{
			final Path sourceOutputPath = sourceOutput.resolvePath(moduleName);
			final Path classOutputPath = classOutput.resolvePath(moduleName);
			final Path sourcePath = moduleRoot.resolvePath(moduleName);

			// TODO: XXX add all modules to source, or how else to manage dependencies?;

			javaModuleCompiler.compile(additionalClassPath, sourcePath, sourceOutputPath, classOutputPath, processor);
		}
	}

	@NotNull
	private static Path createTemporaryClassOutputPath()
	{
		final Path classOutputRootPath;
		try
		{
			classOutputRootPath = createTempDirectory(TemporaryFolderPrefix);
		}
		catch (final IOException e)
		{
			throw new IllegalStateException(format("Could not create a temporary folder for java2c because of '%1$s'", e.getMessage()), e);
		}
		return classOutputRootPath;
	}

	@NotNull
	private static JavaCompiler getJavaCompiler()
	{
		@Nullable final JavaCompiler javaCompiler = getSystemJavaCompiler();
		if (javaCompiler == null)
		{
			throw new IllegalStateException("There is no system Java compiler");
		}
		return javaCompiler;
	}
}
