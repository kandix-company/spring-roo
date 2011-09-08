package org.springframework.roo.classpath.scanner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.roo.classpath.details.AbstractMemberHoldingTypeDetailsBuilder;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.ConstructorMetadata;
import org.springframework.roo.classpath.details.ConstructorMetadataBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.model.CustomData;
import org.springframework.roo.model.CustomDataBuilder;
import org.springframework.roo.model.CustomDataKey;

/**
 * Builder for {@link MemberDetails}.
 *
 * @author Alan Stewart
 * @author Ben Alex
 * @author James Tyrrell
 * @since 1.1.3
 */
public class MemberDetailsBuilder {
	
	// Fields
	private Map<String, MemberHoldingTypeDetails> memberHoldingTypeDetailsMap = new LinkedHashMap<String, MemberHoldingTypeDetails>();
	private Map<String, TypeDetailsBuilder> typeDetailsBuilderMap = new LinkedHashMap<String, TypeDetailsBuilder>();
	private MemberDetails originalMemberDetails;
	private boolean changed = false;

	public MemberDetailsBuilder(MemberDetails memberDetails) {
		this.originalMemberDetails = memberDetails;
		for (MemberHoldingTypeDetails memberHoldingTypeDetails : memberDetails.getDetails()) {
			memberHoldingTypeDetailsMap.put(memberHoldingTypeDetails.getDeclaredByMetadataId(), memberHoldingTypeDetails);
		}
	}

	public MemberDetailsBuilder(List<MemberHoldingTypeDetails> memberHoldingTypeDetailsList) {
		this.originalMemberDetails = new MemberDetailsImpl(memberHoldingTypeDetailsList);
		for (MemberHoldingTypeDetails memberHoldingTypeDetails : originalMemberDetails.getDetails()) {
			memberHoldingTypeDetailsMap.put(memberHoldingTypeDetails.getDeclaredByMetadataId(), memberHoldingTypeDetails);
		}
	}

	public MemberDetails build() {
		if (changed) {
			for (TypeDetailsBuilder typeDetailsBuilder : typeDetailsBuilderMap.values()) {
				memberHoldingTypeDetailsMap.put(typeDetailsBuilder.getDeclaredByMetadataId(), typeDetailsBuilder.build());
			}
			return new MemberDetailsImpl(new ArrayList<MemberHoldingTypeDetails>(memberHoldingTypeDetailsMap.values()));
		}
		return originalMemberDetails;
	}

	public <T> void tag(T toModify, CustomDataKey<T> key, Object value) {
		if (toModify instanceof FieldMetadata) {
			CustomDataBuilder customDataBuilder = new CustomDataBuilder();
			customDataBuilder.put(key, value);
			doModification((FieldMetadata) toModify, customDataBuilder.build());
		} else if (toModify instanceof MethodMetadata) {
			CustomDataBuilder customDataBuilder = new CustomDataBuilder();
			customDataBuilder.put(key, value);
			doModification((MethodMetadata) toModify, customDataBuilder.build());
		} else if (toModify instanceof ConstructorMetadata) {
			CustomDataBuilder customDataBuilder = new CustomDataBuilder();
			customDataBuilder.put(key, value);
			doModification((ConstructorMetadata) toModify, customDataBuilder.build());
		} else if (toModify instanceof MemberHoldingTypeDetails) {
			CustomDataBuilder customDataBuilder = new CustomDataBuilder();
			customDataBuilder.put(key, value);
			doModification((MemberHoldingTypeDetails) toModify, customDataBuilder.build());
		}
	}

	private void doModification(FieldMetadata field, CustomData customData) {
		MemberHoldingTypeDetails memberHoldingTypeDetails = memberHoldingTypeDetailsMap.get(field.getDeclaredByMetadataId());
		if (memberHoldingTypeDetails != null) {
			FieldMetadata matchedField = MemberFindingUtils.getField(memberHoldingTypeDetails, field.getFieldName());
			if (matchedField != null && !matchedField.getCustomData().keySet().containsAll(customData.keySet())) {
				TypeDetailsBuilder typeDetailsBuilder = getTypeDetailsBuilder(memberHoldingTypeDetails);
				typeDetailsBuilder.addDataToField(field, customData);
				changed = true;
			}
		}
	}

	private void doModification(MethodMetadata method, CustomData customData) {
		MemberHoldingTypeDetails memberHoldingTypeDetails = memberHoldingTypeDetailsMap.get(method.getDeclaredByMetadataId());
		if (memberHoldingTypeDetails != null) {
			MethodMetadata matchedMethod = MemberFindingUtils.getMethod(memberHoldingTypeDetails, method.getMethodName(), AnnotatedJavaType.convertFromAnnotatedJavaTypes(method.getParameterTypes()));
			if (matchedMethod != null && !matchedMethod.getCustomData().keySet().containsAll(customData.keySet())) {
				TypeDetailsBuilder typeDetailsBuilder = getTypeDetailsBuilder(memberHoldingTypeDetails);
				typeDetailsBuilder.addDataToMethod(method, customData);
				changed = true;
			}
		}
	}

	private void doModification(ConstructorMetadata constructor, CustomData customData) {
		MemberHoldingTypeDetails memberHoldingTypeDetails = memberHoldingTypeDetailsMap.get(constructor.getDeclaredByMetadataId());
		if (memberHoldingTypeDetails != null) {
			ConstructorMetadata matchedConstructor = MemberFindingUtils.getDeclaredConstructor(memberHoldingTypeDetails, AnnotatedJavaType.convertFromAnnotatedJavaTypes(constructor.getParameterTypes()));
			if (matchedConstructor != null && !matchedConstructor.getCustomData().keySet().containsAll(customData.keySet())) {
				TypeDetailsBuilder typeDetailsBuilder = getTypeDetailsBuilder(memberHoldingTypeDetails);
				typeDetailsBuilder.addDataToConstructor(constructor, customData);
				changed = true;
			}
		}
	}

	private void doModification(MemberHoldingTypeDetails type, CustomData customData) {
		MemberHoldingTypeDetails memberHoldingTypeDetails = memberHoldingTypeDetailsMap.get(type.getDeclaredByMetadataId());
		if (memberHoldingTypeDetails != null) {
			if (memberHoldingTypeDetails.getName().equals(type.getName()) && !memberHoldingTypeDetails.getCustomData().keySet().containsAll(customData.keySet())) {
				TypeDetailsBuilder typeDetailsBuilder = getTypeDetailsBuilder(memberHoldingTypeDetails);
				typeDetailsBuilder.getCustomData().append(customData);
				changed = true;
			}
		}
	}

	private TypeDetailsBuilder getTypeDetailsBuilder(MemberHoldingTypeDetails memberHoldingTypeDetails) {
		if (typeDetailsBuilderMap.containsKey(memberHoldingTypeDetails.getDeclaredByMetadataId())) {
			return typeDetailsBuilderMap.get(memberHoldingTypeDetails.getDeclaredByMetadataId());
		}
		TypeDetailsBuilder typeDetailsBuilder = new TypeDetailsBuilder(memberHoldingTypeDetails);
		typeDetailsBuilderMap.put(memberHoldingTypeDetails.getDeclaredByMetadataId(), typeDetailsBuilder);
		return typeDetailsBuilder;
	}

	static class TypeDetailsBuilder extends AbstractMemberHoldingTypeDetailsBuilder<MemberHoldingTypeDetails> {
		private MemberHoldingTypeDetails existing;

		protected TypeDetailsBuilder(MemberHoldingTypeDetails existing) {
			super(existing);
			this.existing = existing;
		}

		public MemberHoldingTypeDetails build() {
			if (existing instanceof ItdTypeDetails) {
				ItdTypeDetailsBuilder builder = new ItdTypeDetailsBuilder((ItdTypeDetails) existing);
				// Push in all members that may have been modified
				builder.setDeclaredFields(this.getDeclaredFields());
				builder.setDeclaredMethods(this.getDeclaredMethods());
				builder.setAnnotations(this.getAnnotations());
				builder.setCustomData(this.getCustomData());
				builder.setDeclaredConstructors(this.getDeclaredConstructors());
				builder.setDeclaredInitializers(this.getDeclaredInitializers());
				builder.setDeclaredInnerTypes(this.getDeclaredInnerTypes());
				builder.setExtendsTypes(this.getExtendsTypes());
				builder.setImplementsTypes(this.getImplementsTypes());
				builder.setModifier(this.getModifier());
				return builder.build();
			} else if (existing instanceof ClassOrInterfaceTypeDetails) {
				ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder((ClassOrInterfaceTypeDetails) existing);
				// Push in all members that may
				builder.setDeclaredFields(this.getDeclaredFields());
				builder.setDeclaredMethods(this.getDeclaredMethods());
				builder.setAnnotations(this.getAnnotations());
				builder.setCustomData(this.getCustomData());
				builder.setDeclaredConstructors(this.getDeclaredConstructors());
				builder.setDeclaredInitializers(this.getDeclaredInitializers());
				builder.setDeclaredInnerTypes(this.getDeclaredInnerTypes());
				builder.setExtendsTypes(this.getExtendsTypes());
				builder.setImplementsTypes(this.getImplementsTypes());
				builder.setModifier(this.getModifier());
				return builder.build();
			} else {
				throw new IllegalStateException("Unknown instance of MemberHoldingTypeDetails");
			}
		}

		public void addDataToField(FieldMetadata replacement, CustomData customData) {
			// If the MIDs don't match then the proposed can't be a replacement
			if (!replacement.getDeclaredByMetadataId().equals(getDeclaredByMetadataId())) {
				return;
			}
			for (FieldMetadataBuilder existingField : getDeclaredFields()) {
				if (existingField.getFieldName().equals(replacement.getFieldName())) {
					for (Object key : customData.keySet()) {
						existingField.putCustomData(key, customData.get(key));
					}
					break;
				}
			}
		}

		public void addDataToMethod(MethodMetadata replacement, CustomData customData) {
			// If the MIDs don't match then the proposed can't be a replacement
			if (!replacement.getDeclaredByMetadataId().equals(getDeclaredByMetadataId())) {
				return;
			}
			for (MethodMetadataBuilder existingMethod : getDeclaredMethods()) {
				if (existingMethod.getMethodName().equals(replacement.getMethodName())) {
					if (AnnotatedJavaType.convertFromAnnotatedJavaTypes(existingMethod.getParameterTypes()).equals(AnnotatedJavaType.convertFromAnnotatedJavaTypes(replacement.getParameterTypes()))) {
						for (Object key : customData.keySet()) {
							existingMethod.putCustomData(key, customData.get(key));
						}
						break;
					}
				}
			}
		}

		public void addDataToConstructor(ConstructorMetadata replacement, CustomData customData) {
			// If the MIDs don't match then the proposed can't be a replacement
			if (!replacement.getDeclaredByMetadataId().equals(getDeclaredByMetadataId())) {
				return;
			}
			for (ConstructorMetadataBuilder existingConstructor : getDeclaredConstructors()) {
				if (AnnotatedJavaType.convertFromAnnotatedJavaTypes(existingConstructor.getParameterTypes()).equals(AnnotatedJavaType.convertFromAnnotatedJavaTypes(replacement.getParameterTypes()))) {
					for (Object key : customData.keySet()) {
						existingConstructor.putCustomData(key, customData.get(key));
					}
					break;
				}
			}
		}
	}
}
