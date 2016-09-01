package io.protostuff.jetbrains.plugin.annotator;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import io.protostuff.jetbrains.plugin.ProtostuffBundle;
import io.protostuff.jetbrains.plugin.psi.TypeReferenceNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class UnresolvedReferenceAnnotator implements Annotator {

    public static TextAttributesKey ERROR_INFO_ATTR_KEY = TextAttributesKey.createTextAttributesKey("error", CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES);

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof TypeReferenceNode) {
            TypeReferenceNode refNode = (TypeReferenceNode) element;
            if (!refNode.isStandardType()) {
                PsiReference reference = refNode.getReference();
                if (reference.resolve() == null) {
                    String message = ProtostuffBundle.message("error.cannot.resolve", refNode.getText());
                    Annotation annotation = holder.createErrorAnnotation(element.getNode(), message);
                    annotation.setHighlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                }
            }
        }
    }

}
