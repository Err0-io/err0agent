package io.err0.client.core;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;

import java.io.IOException;
import java.nio.file.Path;

public class GitHelper {
    public static boolean isIgnored(Repository repository, String relativePath) throws IOException {
        WorkingTreeIterator treeIterator = new FileTreeIterator(repository);
        try (TreeWalk walk = new TreeWalk(repository)) {
            walk.addTree(treeIterator);
            walk.setFilter(PathFilterGroup.createFromStrings(relativePath));
            while (walk.next()) {
                WorkingTreeIterator workingTreeIterator = walk.getTree(0,
                        WorkingTreeIterator.class);
                if (workingTreeIterator.isEntryIgnored()) {
                    return true;
                }
                if (walk.getPathString().equals(relativePath)) {
                    return false; // workingTreeIterator.isEntryIgnored();
                }
                if (workingTreeIterator.getEntryFileMode()
                        .equals(FileMode.TREE)) {
                    walk.enterSubtree();
                }
            }
        }
        return false;
    }
}
