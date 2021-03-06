package io.protostuff.jetbrains.plugin.psi;

import com.intellij.lang.ASTNode;
import org.antlr.jetbrains.adapter.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class ProtoRootNode extends ANTLRPsiNode implements KeywordsContainer, UserTypeContainer {

    public ProtoRootNode(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * Returns a package name for given proto file.
     * If package is not set, returns an empty string.
     */
    @NotNull
    public String getPackageName() {
        PackageStatement packageStatement = getPackageStatement();
        if (packageStatement != null) {
            return packageStatement.getPackageName();
        }
        return "";
    }


    public UserType resolve(String typeName, Deque<String> scopeLookupList) {
        return resolve(typeName, scopeLookupList, true);
    }

    public UserType resolve(String typeName, Deque<String> scopeLookupList, boolean resolveInImports) {
        UserType result = null;
        // A leading '.' (for example, .foo.bar.Baz) means to start from the outermost scope
        if (typeName.startsWith(".")) {
            result = resolveByQualifiedName(typeName);
        } else {
            for (String scope : scopeLookupList) {
                String fullTypeName = scope + typeName;
                UserType type = resolveByQualifiedName(fullTypeName);
                if (type != null) {
                    result = type;
                    break;
                }
            }
        }
        if (result != null) {
            return result;
        }
        if (!resolveInImports) {
            return null;
        }
        ImportNode[] importNodes = findChildrenByClass(ImportNode.class);
        for (ImportNode importNode : importNodes) {
            ProtoRootNode targetProto = importNode.getTargetProto();
            if (targetProto != null) {
                boolean aPublic = importNode.isPublic();
                result = targetProto.resolve(typeName, scopeLookupList, aPublic);
                if (result != null) {
                    return result;
                }

            }
        }
        return null;
    }

    public UserType resolveByQualifiedName(String qualifiedName) {
        String prefix = getCurrentProtoPrefix();
        if (qualifiedName.startsWith(prefix)) {
            return resolveRecursive(this, qualifiedName);
        }
        return null;
    }

    @NotNull
    private String getCurrentProtoPrefix() {
        String packageName = getPackageName();
        if ("".equals(packageName)) {
            return ".";
        }
        return "." + getPackageName() + ".";
    }

    public UserType resolveRecursive(UserTypeContainer container, String targetName) {
        Collection<UserType> childrenTypes = container.getChildrenTypes();
        for (UserType type : childrenTypes) {
            String qualifiedName = type.getQualifiedName();
            if (Objects.equals(targetName, qualifiedName)) {
                return type;
            }
            if (type instanceof UserTypeContainer && targetName.startsWith(qualifiedName + ".")) {
                return resolveRecursive((UserTypeContainer) type, targetName);
            }
        }
        return null;
    }

    @Nullable
    private PackageStatement getPackageStatement() {
        return findChildByClass(PackageStatement.class);
    }

    @Override
    public String getNamespace() {
        String packageName = getPackageName();
        if (packageName.isEmpty()) {
            return ".";
        }
        return "." + packageName + ".";
    }

    @Override
    public Collection<UserType> getChildrenTypes() {
        return Arrays.asList(findChildrenByClass(UserType.class));
    }
}