package io.protostuff.jetbrains.plugin.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract block for all constructs that have children in curly braces.
 *
 * @author Kostiantyn Shchepanovskyi
 */
class ParentBlock extends StatementBlock {

    private final Set<Block> headerBlocks = new HashSet<>();
    private Alignment childAlignment;

    ParentBlock(@NotNull ASTNode node, @Nullable Alignment alignment,
                Indent indent, CodeStyleSettings settings) {
        super(node, alignment, indent, settings);
        childAlignment = Alignment.createAlignment();
    }

    @Override
    protected List<Block> buildChildren() {
        ASTNode child = getNode().getFirstChildNode();
        State state = State.BEFORE_LEFT_CURLY_BRACE;
        List<Block> result = new ArrayList<>();
        while (child != null) {
            if (!FormatterUtil.containsWhiteSpacesOnly(child)) {
                IElementType elementType = child.getElementType();
                if (LCURLY.equals(elementType)) {
                    state = State.AFTER_LEFT_CURLY_BRACE;
                    result.add(BlockFactory.createBlock(child, myAlignment, Indent.getNoneIndent(), settings));
                } else if (RCURLY.equals(elementType)) {
                    result.add(BlockFactory.createBlock(child, myAlignment, Indent.getNoneIndent(), settings));
                    state = State.AFTER_RIGHT_CURLY_BRACE;
                } else {
                    switch (state) {
                        case BEFORE_LEFT_CURLY_BRACE:
                            Block block = BlockFactory.createBlock(child, myAlignment, Indent.getNoneIndent(), settings);
                            headerBlocks.add(block);
                            result.add(block);
                            break;
                        case AFTER_LEFT_CURLY_BRACE:
                            result.add(BlockFactory.createBlock(child, childAlignment, Indent.getNormalIndent(true), settings));
                            break;
                        case AFTER_RIGHT_CURLY_BRACE:
                            result.add(BlockFactory.createBlock(child, myAlignment, Indent.getNoneIndent(), settings));
                            break;
                        default:
                            throw new IllegalStateException(state.toString());
                    }
                }
            }
            child = child.getTreeNext();
        }
        return result;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        if (child2 instanceof ASTBlock) {
            ASTBlock block = (ASTBlock) child2;
            // Do not move semicolon after '}' to new line.
            IElementType elementType = block.getNode().getElementType();
            if (SEMICOLON.equals(elementType)) {
                return NONE;
            }
            // Do not move trailing comments to new line.
            if (LINE_COMMENT.equals(elementType)
                    || COMMENT.equals(elementType)) {
                return SPACE_OR_NEW_LINE;
            }
        }
        if (headerBlocks.contains(child1)) {
            return super.getSpacing(child1, child2);
        }
        return NEW_LINE;
    }

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
        return new ChildAttributes(getChildIndent(), childAlignment);
    }

    @Nullable
    @Override
    protected Indent getChildIndent() {
        return Indent.getNormalIndent();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    private enum State {
        BEFORE_LEFT_CURLY_BRACE,
        AFTER_LEFT_CURLY_BRACE,
        AFTER_RIGHT_CURLY_BRACE
    }
}
