package io.protostuff.jetbrains.plugin.psi;

import com.intellij.lang.ASTNode;
import org.antlr.jetbrains.adapter.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;

import static io.protostuff.jetbrains.plugin.ProtoParserDefinition.R_FIELD_NAME;
import static io.protostuff.jetbrains.plugin.ProtoParserDefinition.R_TAG;
import static io.protostuff.jetbrains.plugin.psi.Util.decodeIntegerFromText;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class MapNode extends ANTLRPsiNode
        implements MessageField, KeywordsContainer {

    public MapNode(@NotNull ASTNode node) {
        super(node);
    }


    @Override
    public String getFieldName() {
        ASTNode nameNode = getFieldNameNode();
        if (nameNode != null) {
            return nameNode.getText();
        }
        return "";
    }

    @Override
    public ASTNode getFieldNameNode() {
        ASTNode node = getNode();
        return node.findChildByType(R_FIELD_NAME);
    }

    @Override
    public int getTag() {
        ASTNode tagNode = getTagNode();
        return decodeIntegerFromText(tagNode);
    }

    @Override
    public ASTNode getTagNode() {
        ASTNode node = getNode();
        return node.findChildByType(R_TAG);
    }

}
