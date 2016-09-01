package io.protostuff.jetbrains.plugin.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.IElementType;
import io.protostuff.jetbrains.plugin.ProtoParserDefinition;
import io.protostuff.jetbrains.plugin.reference.TypeReference;
import org.antlr.jetbrains.adapter.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class TypeReferenceNode extends ANTLRPsiNode implements KeywordsContainer {

    public TypeReferenceNode(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public PsiReference getReference() {
        return new TypeReference(this, TextRange.create(0, getNode().getTextLength()));
    }

    public boolean isStandardType() {
        IElementType innerElementType = getFirstChild().getNode().getElementType();
        return ProtoParserDefinition.SCALAR_TYPES.contains(innerElementType);
    }
}