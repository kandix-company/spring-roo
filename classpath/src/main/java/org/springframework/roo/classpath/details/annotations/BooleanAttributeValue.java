package org.springframework.roo.classpath.details.annotations;

import org.springframework.roo.model.JavaSymbolName;

/**
 * Represents a boolean annotation attribute value.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public class BooleanAttributeValue extends AbstractAnnotationAttributeValue<Boolean> {
	
	// Fields
	private final boolean value;
	
	/**
	 * Constructor
	 *
	 * @param name
	 * @param value
	 */
	public BooleanAttributeValue(JavaSymbolName name, boolean value) {
		super(name);
		this.value = value;
	}

	public Boolean getValue() {
		return value;
	}
	
	public String toString() {
		return getName() + " -> " + value;
	}
}
