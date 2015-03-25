package com.java2c.transpiler.fieldAttributesProcessors;

import com.java2c.transpiler.c.gccAttributes.GccAttribute;
import com.java2c.transpiler.c.gccAttributes.variable.GccVariableAttributeName;
import com.java2c.transpiler.elementConverters.ConversionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.VariableElement;
import java.util.Collection;

public final class NullCheckFieldAttributesProcessor extends AbstractFieldAttributesProcessor
{
	@NotNull public static final FieldAttributesProcessor NullCheck = new NullCheckFieldAttributesProcessor();

	private NullCheckFieldAttributesProcessor()
	{
	}

	@Override
	public void processField(@NotNull final Collection<GccAttribute<GccVariableAttributeName>> gccAttributes, @NotNull final VariableElement field) throws ConversionException
	{
		final boolean isNullable = hasAnnotation(field, Nullable.class);
		final boolean isNotNull = hasAnnotation(field, NotNull.class);
		if (isNullable && isNotNull)
		{
			throw newConversionException(field, "may not be marked as @Nullable and @NotNull");
		}
	}
}