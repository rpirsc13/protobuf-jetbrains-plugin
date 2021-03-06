package io.protostuff.jetbrains.plugin.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;
import io.protostuff.jetbrains.plugin.psi.UserType;
import io.protostuff.jetbrains.plugin.psi.UserTypeContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class TypeReference extends PsiReferenceBase<PsiElement> {

    private String key;

    public TypeReference(PsiElement element, TextRange textRange) {
        super(element, textRange, true);
        key = element.getText();
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return resolveInScope(getElement(), this);
    }

    public static UserType resolveInScope(PsiElement scopeElement, TypeReference ref) {
        PsiElement element = scopeElement;
        while (element != null && !(element instanceof UserTypeContainer)) {
            element = element.getParent();
        }
        PsiElement protoElement = element;
        while (protoElement != null && !(protoElement instanceof ProtoRootNode)) {
            protoElement = protoElement.getParent();
        }
        if (element == null || protoElement == null) {
            return null;
        }
        UserTypeContainer scope = (UserTypeContainer) element;
        ProtoRootNode proto = (ProtoRootNode) protoElement;
        Deque<String> scopeLookupList = createScopeLookupList(scope);
        return proto.resolve(ref.key, scopeLookupList);
    }

    // Type name resolution in the protocol buffer language works like C++: first
    // the innermost scope is searched, then the next-innermost, and so on, with
    // each package considered to be "inner" to its parent package.
    public static Deque<String> createScopeLookupList(UserTypeContainer container) {
        String namespace = container.getNamespace();
        Deque<String> scopeLookupList = new ArrayDeque<>();
        int end = 0;
        while (end >= 0) {
            end = namespace.indexOf('.', end);
            if (end >= 0) {
                end++;
                String scope = namespace.substring(0, end);
                scopeLookupList.addFirst(scope);
            }
        }
        return scopeLookupList;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
