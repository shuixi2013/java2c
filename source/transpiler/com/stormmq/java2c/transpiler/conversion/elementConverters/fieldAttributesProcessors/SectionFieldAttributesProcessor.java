package com.stormmq.java2c.transpiler.conversion.elementConverters.fieldAttributesProcessors;

import com.stormmq.java2c.model.variables.section;
import com.stormmq.java2c.transpiler.conversion.c.gccAttributes.GccAttribute;
import com.stormmq.java2c.transpiler.conversion.c.gccAttributes.GccAttributeParameter;
import com.stormmq.java2c.transpiler.conversion.c.gccAttributes.variable.GccVariableAttributeName;
import com.stormmq.java2c.transpiler.conversion.elementConverters.ConversionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.VariableElement;
import java.util.Collection;

public final class SectionFieldAttributesProcessor extends AbstractFieldAttributesProcessor
{
	@NotNull public static final FieldAttributesProcessor Section = new SectionFieldAttributesProcessor();

	private SectionFieldAttributesProcessor()
	{
	}

	@Override
	public void processField(@NotNull final Collection<GccAttribute<GccVariableAttributeName>> gccAttributes, @NotNull final VariableElement field) throws ConversionException
	{
		@Nullable final section section = field.getAnnotation(section.class);
		if (section == null)
		{
			return;
		}

		@Nullable final String sectionName = section.value();
		if (sectionName == null)
		{
			throw new ConversionException("@section must specify a section name (value)");
		}
		gccAttributes.add(new GccAttribute<>(GccVariableAttributeName.section, new GccAttributeParameter(sectionName)));
	}
}
