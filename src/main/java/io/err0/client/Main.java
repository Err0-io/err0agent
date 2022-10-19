/*
Copyright 2022 BlueTrailSoftware, Holding Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.err0.client;

import io.err0.client.core.*;
import io.err0.client.languages.*;
import io.err0.client.rules.ExceptionRule;
import io.err0.client.rules.ExceptionRuleAction;
import io.err0.client.rules.ExceptionRuleOperation;
import io.err0.client.rules.ExceptionRuleSelection;
import io.err0.client.test.UnitTestApiProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {

    public final static boolean USE_NEAREST_CODE_FOR_LINE_OF_CODE = true;
    public final static int CHAR_RADIUS = 4*1024;
    private static Pattern reGitdir = Pattern.compile("^gitdir: (.*?)$", Pattern.MULTILINE);

    static class GitMetadata {
        GitMetadata(final String gitHash, final boolean statusIsClean, final boolean detachedHead) {
            this.gitHash = gitHash;
            this.statusIsClean = statusIsClean;
            this.detachedHead = detachedHead;
        }
        final String gitHash;
        final boolean statusIsClean;
        final boolean detachedHead;
    }

    private static GitMetadata populateGitMetadata(final String checkoutDir, final JsonObject appGitMetadata, final JsonObject runGitMetadata) throws IOException, GitAPIException {
        JsonArray remotes = new JsonArray();
        JsonObject branches = new JsonObject();
        JsonObject tags = new JsonObject();
        JsonObject tag_annotations = new JsonObject();
        /*
        JsonObject branch_tags = new JsonObject();
        JsonObject tag_branches = new JsonObject();
         */
        appGitMetadata.add("remotes", remotes);
        appGitMetadata.add("branches", branches);
        appGitMetadata.add("tags", tags);
        appGitMetadata.add("tag_annotations", tag_annotations);
        /*
        appGitMetadata.add("branch_tags", branch_tags);
        appGitMetadata.add("tag_branches", tag_branches);
         */

        Path gitpath = Path.of(checkoutDir + "/.git");
        if (Files.isRegularFile(gitpath)) {
            final String contents = Files.readString(gitpath);
            Matcher matcher = reGitdir.matcher(contents);
            if (matcher.find()) {
                gitpath = Path.of(checkoutDir + "/" + matcher.group(1));
            }
        }

        // find git version etc.
        Repository repo = new FileRepositoryBuilder()
                .setGitDir(new File(gitpath.toAbsolutePath().toString()))
                .build();

        boolean detachedHead = true;
        final String currentFullBranch = repo.getFullBranch();
        if (null != currentFullBranch && currentFullBranch.startsWith("refs/heads/")) {
            runGitMetadata.addProperty("current_branch", currentFullBranch.substring(11));
            detachedHead = false;
        }

        ObjectId obj = repo.resolve("HEAD");
        String gitHash = ObjectId.toString(obj);

        // TODO: better solution
        if (null != gitHash && "0000000000000000000000000000000000000000".equals(gitHash)) {
            gitHash = null;
        }

        final String objectId = gitHash;

        runGitMetadata.addProperty("git_hash", gitHash);

        Git git = Git.wrap(repo);
        RevWalk walk = new RevWalk(repo);
        if (null != gitHash) {
            JsonArray gitTags = new JsonArray();
            List<Ref> tagList = git.tagList().call();
            for (Ref ref : tagList) {
                final String fullTagName = ref.getName();
                if (fullTagName.startsWith("refs/tags/")) {
                    final String tagName = fullTagName.substring(10);
                    ObjectId commitObjectId = ref.getObjectId();
                    String tagObjectId = ObjectId.toString(commitObjectId);
                    RevTag tag = null;
                    try {
                        tag = walk.parseTag(ref.getObjectId());
                        commitObjectId = tag.getObject().getId();
                        tagObjectId = ObjectId.toString(commitObjectId);
                        // ref points to an annotated tag
                    } catch(IncorrectObjectTypeException notAnAnnotatedTag) {
                        // ref is a lightweight (aka unannotated) tag
                        tag = null;
                    }

                    if (tagObjectId.equals(objectId)) {
                        gitTags.add(tagName);
                    }
                    tags.addProperty(tagName, tagObjectId);
                    if (null != tag) {
                        JsonObject annotation = new JsonObject();
                        annotation.addProperty("message", tag.getFullMessage());
                        PersonIdent tagger = tag.getTaggerIdent();
                        annotation.addProperty("name", tagger.getName());
                        annotation.addProperty("email_address", tagger.getEmailAddress());
                        annotation.addProperty("when", tagger.getWhen().getTime());
                        annotation.addProperty("tz_offset", tagger.getTimeZoneOffset());
                        tag_annotations.add(tagName, annotation);
                    }

                    /*
                    DISABLED: too much information, also slow, also gets slower each iteration of the soak test.

                    List<Ref> refs = git.branchList().setContains(tagObjectId).setListMode(ListBranchCommand.ListMode.ALL).call();
                    refs.forEach(branchRef -> {
                        final String fullBranchName = branchRef.getName();
                        if (fullBranchName.startsWith("refs/heads/")) {
                            final String branchName = fullBranchName.substring(11);
                            JsonArray a = null;
                            if (branch_tags.has(branchName)) {
                                a = branch_tags.getAsJsonArray(branchName);
                            } else {
                                a = new JsonArray();
                                branch_tags.add(branchName, a);
                            }
                            a.add(tagName);
                            a = null;
                            if (tag_branches.has(tagName)) {
                                a = tag_branches.getAsJsonArray(tagName);
                            } else {
                                a = new JsonArray();
                                tag_branches.add(tagName, a);
                            }
                            a.add(branchName);
                        }
                    });
                     */
                }
            }
            HashSet<String> dedupe = new HashSet<>();
            JsonArray gitBranches = new JsonArray();
            List<Ref> branchList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            for (Ref ref : branchList) {
                final String fullBranchName = ref.getName();
                if (fullBranchName.startsWith("refs/heads/")) {
                    final String branchName = fullBranchName.substring(11);
                    if (! "HEAD".equals(branchName) && dedupe.add(branchName)) {
                        final String branchObjectId = ObjectId.toString(ref.getObjectId());
                        if (branchObjectId.equals(objectId)) {
                            gitBranches.add(branchName);
                        }
                        branches.addProperty(branchName, branchObjectId);
                    }
                }
                else if (fullBranchName.startsWith("refs/remotes/")) {
                    final String remoteNameBranchName = fullBranchName.substring(13);
                    final int i = remoteNameBranchName.indexOf('/');
                    if (i >= 0) {
                        final String branchName = remoteNameBranchName.substring(i+1);
                        if (! "HEAD".equals(branchName) && dedupe.add(branchName)) {
                            final String branchObjectId = ObjectId.toString(ref.getObjectId());
                            if (branchObjectId.equals(objectId)) {
                                gitBranches.add(branchName);
                            }
                            branches.addProperty(branchName, branchObjectId);
                        }
                    }
                }
            }
            runGitMetadata.add("git_tags", gitTags);
            runGitMetadata.add("git_branches", gitBranches);

            List<RemoteConfig> remoteList = git.remoteList().call();
            for (RemoteConfig rc : remoteList) {
                JsonObject remote = new JsonObject();
                remote.addProperty("name", rc.getName());
                JsonArray uris = new JsonArray();
                for (URIish ish : rc.getURIs()) {
                    uris.add(ish.toASCIIString());
                }
                remote.add("uris", uris);
                remotes.add(remote);
            }
        }
        Status status = git.status().call();
        boolean statusIsClean = status.isClean();

        if (! statusIsClean) {
            // repo is dirty before agent run, flag hash as dirty and remove tags from this run's metadata.
            if (null != gitHash) {
                runGitMetadata.addProperty("git_hash", gitHash + "-dirty");
            }
            runGitMetadata.add("git_tags", new JsonArray());
        }

        return new GitMetadata(gitHash, statusIsClean, detachedHead);
    }

    public static void main(String args[]) {

        ApiProvider apiProvider = null;
        ResultDriver driver = new FileResultDriver();

        RealmPolicy realmPolicy = null;
        ProjectPolicy projectPolicy = null;

        try {

            for (int i = 0, l = args.length; i < l; ++i) {
                String arg = args[i];
                if ("--help".equals(arg)) {
                    System.out.println("[AGENT-000035] Usage");
                    System.out.println("[AGENT-000036] <command> --token path-to-token.json --insert /path/to/git/repo");
                    System.out.println("[AGENT-000037] insert error codes into the source code");
                    System.out.println("[AGENT-000038] <command> --token path-to-token.json --analyse --check /path/to/git/repo");
                    System.out.println("[AGENT-000039] analyse error codes in the project and return failure if some need to change");
                }
                else if ("--token".equals(arg)) {

                    // a new API provider per token
                    if (apiProvider != null) {
                        apiProvider.close();
                        apiProvider = null;
                    }
                    projectPolicy = null;
                    realmPolicy = null;

                    apiProvider = new RestApiProvider(args[++i]);
                    RestApiProvider restApiProvider = (RestApiProvider) apiProvider;

                    AtomicReference<RealmPolicy> arRealmPolicy = new AtomicReference<>();
                    AtomicReference<ProjectPolicy> arApplicationPolicy = new AtomicReference<>();

                    // Download the policies...
                    restApiProvider.getPolicy(responseJson -> {
                        if (GsonHelper.asBoolean(responseJson, "success", false)) {
                            arRealmPolicy.set(new RealmPolicy(responseJson.get("realm").getAsJsonObject()));
                            arApplicationPolicy.set(new ProjectPolicy(arRealmPolicy.get(), responseJson.get("app").getAsJsonObject()));
                        } else {
                            RestApiProvider.JsonFormattedExceptionHelper.formatToStderrAndFail(responseJson);
                        }
                    });

                    realmPolicy = arRealmPolicy.get();
                    projectPolicy = arApplicationPolicy.get();
                    // We're ready!

                } else if ("--checkout".equals(arg) || "--insert".equals(arg)) {
                    if (null == realmPolicy) throw new Exception("[AGENT-000001] Must specify realm policy using --realm before specifying checkout dir");
                    if (null == projectPolicy) throw new Exception("[AGENT-000002] Must specify application policy using --app before specifying checkout dir");
                    String checkoutDir = args[++i];
                    boolean importCodes = false;
                    if ("--import".equals(checkoutDir)) {
                        checkoutDir = args[++i];
                        importCodes = true;
                        if (! (apiProvider instanceof UnitTestApiProvider)) {
                            throw new RuntimeException("[AGENT-000003] Only compatible with the unit test api provider.");
                        }
                    }

                    boolean doRenumber = false;
                    if ("--renumber".equals(checkoutDir)) {
                        checkoutDir = args[++i];
                        doRenumber = true;
                    }

                    if (projectPolicy.renumber_on_next_run) {
                        doRenumber = true;
                        if (! apiProvider.markRenumberingOK(projectPolicy)) {
                            throw new RuntimeException("[AGENT-000027] Unable to renumber automatically.");
                        }
                    }

                    final GlobalState globalState = new GlobalState();

                    apiProvider.ensurePolicyIsSetUp(projectPolicy);

                    JsonObject appGitMetadata = new JsonObject();
                    JsonObject runGitMetadata = new JsonObject();
                    final GitMetadata gitMetadata = populateGitMetadata(checkoutDir, appGitMetadata, runGitMetadata);
                    if (gitMetadata.detachedHead) {
                        System.err.println("[AGENT-000040] Detached HEAD in the git repository.");
                        System.exit(-1);
                    }

                    final String gitHash = gitMetadata.gitHash;

                    if (! importCodes) {
                        apiProvider.importPreviousState(projectPolicy, globalState, GsonHelper.asString(runGitMetadata, "current_branch", null));
                    }

                    final UUID run_uuid = apiProvider.createRun(projectPolicy, appGitMetadata, runGitMetadata, "insert");

                    final StatisticsGatherer statisticsGatherer = new StatisticsGatherer();
                    boolean didChangeAFile = false;

                    try {

                        scan(projectPolicy, globalState, checkoutDir, apiProvider, doRenumber);

                        if (importCodes) {
                            _import(apiProvider, globalState, projectPolicy);
                        }


                        didChangeAFile = runInsert(apiProvider, globalState, projectPolicy, driver, run_uuid, statisticsGatherer);
                    }
                    catch (Throwable t) {
                        statisticsGatherer.throwable = t;
                    }

                    if (didChangeAFile) {
                        // flag the hash as dirty, we changed files
                        if (null != gitHash && !gitHash.endsWith("-dirty")) {
                            runGitMetadata.addProperty("git_hash", gitHash + "-dirty");
                        }
                        // remove tags, it no longer corresponds to the tagged version
                        runGitMetadata.add("git_tags", new JsonArray());
                    }

                    apiProvider.updateRun(projectPolicy, run_uuid, runGitMetadata, statisticsGatherer.toRunMetadata());

                    if (null != statisticsGatherer.throwable) {
                        System.err.println(statisticsGatherer.throwable.getMessage());
                        System.exit(-1);
                    }

                } if ("--report".equals(arg) || "--analyse".equals(arg) || "--analyze".equals(arg)) {
                    if (null == realmPolicy) throw new Exception("[AGENT-000004] Must specify realm policy using --realm before specifying report dir");
                    if (null == projectPolicy) throw new Exception("[AGENT-000005] Must specify application policy using --app before specifying report dir");
                    String reportDir = args[++i];
                    String current_branch = null;
                    boolean check = false;
                    boolean dirty = false;
                    if ("--branch".equals(reportDir)) {
                        current_branch = args[++i];
                        reportDir = args[++i];
                    }
                    if ("--dirty".equals(reportDir)) {
                        reportDir = args[++i];
                        dirty = true;
                    }
                    if ("--check".equals(reportDir)) {
                        reportDir = args[++i];
                        check = true;
                        if ((apiProvider instanceof UnitTestApiProvider)) {
                            throw new RuntimeException("[AGENT-000006] Not compatible with the unit test api provider.");
                        }
                    }

                    final GlobalState globalState = new GlobalState();

                    apiProvider.ensurePolicyIsSetUp(projectPolicy);

                    JsonObject appGitMetadata = new JsonObject();
                    JsonObject runGitMetadata = new JsonObject();
                    final GitMetadata gitMetadata = populateGitMetadata(reportDir, appGitMetadata, runGitMetadata);

                    if (null != current_branch && !"".equals(current_branch)) {
                        // override current_branch setting
                        runGitMetadata.addProperty("current_branch", current_branch);
                    }

                    if (null == current_branch && gitMetadata.detachedHead) {
                        System.err.println("[AGENT-000041] Detached HEAD in the git repository; current branch must be specified by --branch.");
                        System.exit(-1);
                    }
                    if (! dirty) {
                        if (!gitMetadata.statusIsClean) {
                            System.err.println("[AGENT-000042] --analyse requires a clean git checkout.");
                            System.exit(-1);
                        }
                    }

                    apiProvider.importPreviousState(projectPolicy, globalState, GsonHelper.asString(runGitMetadata, "current_branch", null));

                    final UUID run_uuid = apiProvider.createRun(projectPolicy, appGitMetadata, runGitMetadata, "analyse");

                    final StatisticsGatherer statisticsGatherer = new StatisticsGatherer();
                    boolean wouldChangeAFile = true;
                    try {
                        scan(projectPolicy, globalState, reportDir, apiProvider, false);

                        wouldChangeAFile = runAnalyse(apiProvider, globalState, projectPolicy, driver, run_uuid, statisticsGatherer);
                    }
                    catch (Throwable t) {
                        statisticsGatherer.throwable = t;
                    }

                    apiProvider.updateRun(projectPolicy, run_uuid, runGitMetadata, statisticsGatherer.toRunMetadata());

                    if (! wouldChangeAFile) {
                        apiProvider.finaliseRun(projectPolicy, run_uuid);
                    }

                    if (wouldChangeAFile) {
                        if (check) {
                            System.err.println("[AGENT-000043] Please run --insert to add missing error codes and retry.");
                            System.exit(-1);
                        } else {
                            System.out.println("[AGENT-000044] Some error codes are missing.");
                        }
                    }

                    if (null != statisticsGatherer.throwable) {
                        System.err.println(statisticsGatherer.throwable.getMessage());
                        System.exit(-1);
                    }

                } else if ("--unit-test-provider".equals(arg)) {
                    if (apiProvider != null) {
                        apiProvider.close();
                        apiProvider = null;
                    }
                    apiProvider = new UnitTestApiProvider();
                }
            }
        }
        catch (Throwable t) {
            System.err.println("[AGENT-000045] Fatal error: " + t.toString());
            t.printStackTrace(System.err);
            System.exit(-1);
        }
        finally {
            if (apiProvider != null) {
                apiProvider.close();
                apiProvider = null;
            }
        }
    }

    public static void _import(final ApiProvider apiProvider, final GlobalState globalState, final ProjectPolicy policy) {
        final String fileNamesInOrder[] = new String[globalState.files.size()];
        globalState.files.keySet().toArray(fileNamesInOrder);
        Arrays.sort(fileNamesInOrder);
        for (int i = 0, l = fileNamesInOrder.length; i < l; ++i) {
            final String filename = fileNamesInOrder[i];

            final StateItem stateItem = globalState.files.get(filename);
            final SourceCodeParse parse = stateItem.parse;

            Token lastToken = null;
            Token currentToken = null;

            for (int j = 0, m = parse.tokenList.size(); j < m; ++j) {
                lastToken = currentToken;
                currentToken = parse.tokenList.get(j);

                if ((
                        parse.couldContainErrorNumber(currentToken)
                ) &&
                        null != lastToken &&
                        lastToken.type == TokenClassification.SOURCE_CODE
                ) {
                    // run, if needed, classification logic on these tokens
                    parse.classifyForErrorCode(apiProvider, globalState, policy, stateItem, currentToken);

                    if (currentToken.classification == Token.Classification.ERROR_NUMBER ||
                            currentToken.classification == Token.Classification.POTENTIAL_ERROR_NUMBER
                    ) {
                        // now we may need to know what this particular token evaluates to
                        parse.classifyForErrorCode(apiProvider, globalState, policy, stateItem, lastToken);
                    }
                }
            }
        }

        AtomicLong maxErrorNumber = new AtomicLong(0);
        globalState.errorCodeMap.forEach((errorOrdinal, tokenStateItem) -> {
            if (errorOrdinal.longValue() > maxErrorNumber.get()) { maxErrorNumber.set(errorOrdinal); }
        });
        long max = maxErrorNumber.get();
        if (max > 0) {
            apiProvider.setNextErrorNumber(policy, max + 1);
        }
    }

    public static void scan(final ProjectPolicy projectPolicy, final GlobalState globalState, String path, ApiProvider apiProvider, boolean doRenumber) {

        if (doRenumber) {
            globalState.previousRunSignatures.clear();
            apiProvider.clearErrorNumberCache(projectPolicy);
        } else {
            apiProvider.cacheAllValidErrorNumbers(projectPolicy);
        }

        path = Paths.get(path).toAbsolutePath().toString();

        System.out.println("[AGENT-000046] Scanning " + path);

        if (! path.endsWith("/")) {
            path = path + "/";
        }
        final String finalPath = path;
        final int lFinalPath = finalPath.length();

        ArrayList<String> excludePaths = new ArrayList<>();
        projectPolicy.excludeDirs.forEach(dir -> {
            if (dir.endsWith("/")) {
                excludePaths.add(finalPath + dir);
            } else {
                excludePaths.add(finalPath + dir + "/");
            }
        });
        int n_exclude_paths = excludePaths.size();
        int n_exclude_patterns = projectPolicy.excludeFilePatterns.size();

        projectPolicy.includeDirs.forEach(dir -> {
            String startPoint = null;
            if (".".equals(dir)) {
                startPoint = finalPath;
            } else {
                startPoint = finalPath + dir;
            }

            final CodePolicy codePolicy = projectPolicy.getCodePolicy();
            final boolean javaAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_java || !codePolicy.adv_java.disable_language);
            final boolean cSharpAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_csharp || !codePolicy.adv_csharp.disable_language);
            final boolean javascriptAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_js || !codePolicy.adv_js.disable_language);
            final boolean typescriptAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_ts || !codePolicy.adv_ts.disable_language);
            final boolean phpAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_php || !codePolicy.adv_php.disable_language);
            final boolean goAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_golang || !codePolicy.adv_golang.disable_language);
            final boolean pythonAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_python || !codePolicy.adv_python.disable_language);

            try (Stream<Path> paths = Files.walk(Paths.get(startPoint)))
            {
                paths.forEach(p -> {
                    final String newFile = p.toString();
                    if (newFile.contains("/.git")) return;
                    for (int i = 0, l = n_exclude_paths; i < l; ++i) {
                        if (newFile.startsWith(excludePaths.get(i))) return;
                    }
                    for (int i = 0, l = n_exclude_patterns; i < l; ++i) {
                        if (projectPolicy.excludeFilePatterns.get(i).matcher(newFile).find()) return;
                    }
                    if (! Files.isDirectory(p)) {
                        if (!newFile.startsWith(finalPath)) {
                            System.err.println("[AGENT-000047] Oops! [" + newFile + "] does not start with [" + finalPath + "]");
                            System.exit(-1);
                        }
                        final String localToCheckoutUnchanged = newFile.substring(lFinalPath);
                        final String localToCheckoutLower = localToCheckoutUnchanged.toLowerCase(Locale.ROOT);

                        final String newFileLower = newFile.toLowerCase(Locale.ROOT);
                        if (javaAllowed && newFileLower.endsWith(".java")) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, JavaSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000048] Parsed: " + newFile);
                        } else if (cSharpAllowed && newFileLower.endsWith(".cs") && !newFileLower.endsWith(".designer.cs") && !newFileLower.endsWith(".generated.cs")) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, CSharpSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000049] Parsed: " + newFile);
                        } else if (javascriptAllowed && ((newFileLower.endsWith(".js") && !newFileLower.endsWith(".min.js")) || newFileLower.endsWith(".jsx"))) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, JavascriptSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000050] Parsed: " + newFile);
                        } else if (typescriptAllowed && ((newFileLower.endsWith(".ts") || newFileLower.endsWith(".tsx")))) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, TypescriptSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000051] Parsed: " + newFile);
                        } else if (phpAllowed && (newFileLower.endsWith(".php") || newFileLower.endsWith(".phtml"))) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, PhpSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000052] Parsed: " + newFile);
                        } else if (goAllowed && newFileLower.endsWith(".go")) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, GolangSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000053] Parsed: " + newFile);
                        } else if (pythonAllowed && newFileLower.endsWith(".py")) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, PythonSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000054] Parsed: " + newFile);
                        }
                    }
                });
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

        });

    }

    public static class FileCoding {
        public final String content;
        public final Charset charset;
        public FileCoding(final Path path) {
            String c = null;
            Charset cs = null;
            try {
                c = Files.readString(path, StandardCharsets.UTF_8);
                cs = StandardCharsets.UTF_8;
            }
            catch (IOException e1) {
                try {
                    c = Files.readString(path, StandardCharsets.ISO_8859_1);
                    cs = StandardCharsets.ISO_8859_1;
                }
                catch (IOException e2) {
                    throw new RuntimeException(e2);
                }
            }
            content = c;
            charset = cs;
        }
    }


    public static boolean runInsert(final ApiProvider apiProvider, final GlobalState globalState, final ProjectPolicy policy, final ResultDriver driver, final UUID run_uuid, final StatisticsGatherer statisticsGatherer) {
        final AnalyseLogic logic = new AnalyseLogic() {

            boolean didChangeAFile = false;

            @Override
            public void pass2ResolveDuplicateErrorNumber(TokenStateItem item, long errorOrdinal) {
                item.token.errorOrdinal = errorOrdinal;
            }

            @Override
            public void pass2AssignNewErrorNumber(TokenStateItem item) {
                item.token.errorOrdinal = apiProvider.nextErrorNumber(policy);
            }

            @Override
            public void pass3AssignNewErrorNumber(Token currentToken) {
                currentToken.errorOrdinal = apiProvider.nextErrorNumber(policy);
            }

            @Override
            public void pass3InsertExistingErrorNumber(StateItem stateItem, Token currentToken) {
                // does nothing
            }

            @Override
            public void pass4CheckIfFileChanged(StateItem stateItem) {
                if (!didChangeAFile && stateItem.getChanged()) { didChangeAFile = true; }
            }

            @Override
            public void pass4ProcessResult(StateItem stateItem, String filename, SourceCodeParse parse) {
                driver.processResult(stateItem.getChanged(), filename, parse, stateItem.fileCharset);
            }

            @Override
            public boolean returnValue() {
                return didChangeAFile;
            }
        };

        return analyseWholeProject(apiProvider, globalState, policy, driver, run_uuid, logic, statisticsGatherer, false);
    }

    public static boolean runAnalyse(final ApiProvider apiProvider, final GlobalState globalState, final ProjectPolicy policy, final ResultDriver driver, final UUID run_uuid, final StatisticsGatherer statisticsGatherer) {

        final AnalyseLogic logic = new AnalyseLogic() {

            boolean wouldChangeAFile = false;

            @Override
            public void pass2ResolveDuplicateErrorNumber(TokenStateItem item, long errorOrdinal) {
                item.token.errorOrdinal = errorOrdinal;
            }

            @Override
            public void pass2AssignNewErrorNumber(TokenStateItem item) {
                item.token.errorOrdinal = 0;
            }

            @Override
            public void pass3AssignNewErrorNumber(Token currentToken) {
                currentToken.errorOrdinal = 0;
            }

            @Override
            public void pass3InsertExistingErrorNumber(StateItem stateItem, Token currentToken) {
                if (!wouldChangeAFile) {
                    wouldChangeAFile = true;
                }
                System.out.println("[AGENT-000055] Analyse would reformat a code in " + stateItem.localToCheckoutUnchanged + " line " + currentToken.startLineNumber);
            }

            @Override
            public void pass4CheckIfFileChanged(StateItem stateItem) {
                if (!wouldChangeAFile && stateItem.getChanged())
                {
                    wouldChangeAFile = true;
                }
                if (stateItem.getChanged()) {
                    System.out.println("[AGENT-000056] Analyse would change " + stateItem.localToCheckoutUnchanged);
                }
            }

            @Override
            public void pass4ProcessResult(StateItem stateItem, String filename, SourceCodeParse parse) {
                // does nothing
            }

            @Override
            public boolean returnValue() {
                return wouldChangeAFile;
            }
        };

        return analyseWholeProject(apiProvider, globalState, policy, driver, run_uuid, logic, statisticsGatherer, true);
    }

    private static Pattern reWhitespace = Pattern.compile("^\\s*$");

    private static boolean analyseWholeProject(final ApiProvider apiProvider, final GlobalState globalState, final ProjectPolicy policy, final ResultDriver driver, final UUID run_uuid, final AnalyseLogic logic, final StatisticsGatherer statisticsGatherer, boolean doNotAssignNewNumbers) {
        final String fileNamesInOrder[] = new String[globalState.files.size()];
        globalState.files.keySet().toArray(fileNamesInOrder);
        Arrays.sort(fileNamesInOrder);
        for (int i = 0, l = fileNamesInOrder.length; i < l; ++i) {
            final String filename = fileNamesInOrder[i];

            //if (! filename.endsWith("StreetStallWebVerticle.java")) continue;
            //if (! filename.endsWith("PublishedSnapshotService.cs")) continue;

            final StateItem stateItem = globalState.files.get(filename);
            final SourceCodeParse parse = stateItem.parse;

            for (int j = 0, m = parse.tokenList.size(); j < m; ++j) {
                final Token lastToken = j > 0 ? parse.tokenList.get(j - 1) : null;
                final Token currentToken = parse.tokenList.get(j);

                if ((
                        parse.couldContainErrorNumber(currentToken)
                        //(sourceType != SourceCodeParse.ParseType.JAVASCRIPT && sourceType != SourceCodeParse.ParseType.GOLANG && sourceType != SourceCodeParse.ParseType.PHP && currentToken.type == ParseItem.QUOT_LITERAL) ||
                        //(sourceType == SourceCodeParse.ParseType.PHP && (currentToken.type == ParseItem.QUOT_LITERAL || currentToken.type == ParseItem.APOS_LITERAL)) ||
                        //((sourceType == SourceCodeParse.ParseType.JAVASCRIPT || sourceType == SourceCodeParse.ParseType.GOLANG) && (currentToken.type == ParseItem.QUOT_LITERAL || currentToken.type == ParseItem.APOS_LITERAL || currentToken.type == ParseItem.BACKTICK_LITERAL))
                ) &&
                        null != lastToken &&
                        lastToken.type == TokenClassification.SOURCE_CODE
                ) {
                    // run, if needed, classification logic on these tokens
                    parse.classifyForErrorCode(apiProvider, globalState, policy, stateItem, currentToken);

                    if (currentToken.classification == Token.Classification.ERROR_NUMBER ||
                            currentToken.classification == Token.Classification.POTENTIAL_ERROR_NUMBER
                    ) {
                        // now we may need to know what this particular token evaluates to
                        parse.classifyForErrorCode(apiProvider, globalState, policy, stateItem, lastToken);

                        if (lastToken.classification == Token.Classification.LOG_OUTPUT ||
                                lastToken.classification == Token.Classification.EXCEPTION_THROW
                        ) {

                            currentToken.insertErrorCode = true;

                            /*

                            In this first pass we don't alter error numbers, we will do that in a second pass...

                            if (! currentToken.keepErrorCode) {
                                // log match
                                currentToken.errorOrdinal = apiProvider.nextErrorNumber();
                                currentToken.source = currentToken.source.substring(0,1) + "[" + policy.getErrorCodeFormatter().formatErrorCode(currentToken.errorOrdinal) + "] " + currentToken.source.substring(1);
                                stateItem.changed = true;
                            } else {
                                final String formatted = currentToken.sourceNoErrorCode.substring(0,1) + "[" + policy.getErrorCodeFormatter().formatErrorCode(currentToken.errorOrdinal) + "] " + currentToken.sourceNoErrorCode.substring(1);
                                if (! formatted.equals(currentToken.source)) {
                                    currentToken.source = formatted;
                                    currentToken.changed = true;
                                    stateItem.changed = true;
                                }
                            }
                            // otherwise no change, keep the error code

                             */
                        } else {

                            // insertErrorCode is false by default.

                            // oops!  we have an error code assigned to a string which isn't a log output or an exception
                            if (null != currentToken.sourceNoErrorCode && currentToken.errorOrdinal >= 0) {
                                globalState.errorCodeMap.get(currentToken.errorOrdinal).removeIf(tokenStateItem -> currentToken == tokenStateItem.token);
                                currentToken.errorOrdinal = -1;
                                currentToken.source = currentToken.sourceNoErrorCode;
                                currentToken.keepErrorCode = false; // always
                            }
                        }

                        // now search for meta-data about this
                        if (currentToken.insertErrorCode) {

                            JsonObject metaData = new JsonObject();
                            JsonArray commentsArray = new JsonArray();
                            JsonArray methodsArray = new JsonArray();

                            // TODO: package name

                            // TODO: method/stack of comments, to go with the "call stack" information

                            // comment immediately preceding

                            ArrayList<Token> commentsReversed = new ArrayList<>();
                            //StringBuilder sbComments = new StringBuilder();
                            for (int k = j - 1; k >= 0; --k) {
                                final Token tok = parse.tokenList.get(k);
                                if (tok.depth == currentToken.depth) {
                                    if (tok.type == TokenClassification.COMMENT_LINE ||
                                            tok.type == TokenClassification.COMMENT_BLOCK
                                    ) {
                                        commentsReversed.add(tok);
                                    }
                                } else {
                                    break;
                                }
                            }
                            for (int x = 0, z = commentsReversed.size(); x < z; ++x) {
                                final Token tok = commentsReversed.get(z - x - 1);
                                final String commentTrimmed = SourceCodeParse.reLeadingWhitespace.matcher(tok.source).replaceAll("").trim();
                                //sbComments.append(commentTrimmed);
                                //if (tok.type == TokenClassification.COMMENT_BLOCK) {
                                //    sbComments.append('\n');
                                //}
                                commentsArray.add(commentTrimmed);
                            }
                            //final String comments = sbComments.toString();

                            // figure out a call stack of sorts class method inner code

                            ArrayList<MethodData> callStackReversed = new ArrayList<>();
                            for (int k = j - 1, depth = currentToken.depth; k >= 0; --k) {
                                final Token tok = parse.tokenList.get(k);
                                if (tok.type == TokenClassification.SOURCE_CODE) {
                                    if (reWhitespace.matcher(tok.source).matches()) {
                                        continue; // skip whitespace
                                    }
                                }
                                if (tok.depth >= depth) continue;
                                if (tok.type == TokenClassification.CONTENT) continue;
                                depth = tok.depth;
                                Token nextTok = null;
                                for (int x = k + 1; x < j; ++x) {
                                    nextTok = parse.tokenList.get(x);
                                    if (nextTok.type != TokenClassification.CONTENT) break;
                                }

                                if (tok.type == TokenClassification.SOURCE_CODE && null != nextTok && nextTok.type == TokenClassification.SOURCE_CODE && tok.depth < nextTok.depth) {
                                    parse.classifyForCallStack(tok);

                                    if (tok.classification == Token.Classification.CLASS_SIGNATURE ||
                                            tok.classification == Token.Classification.METHOD_SIGNATURE ||
                                            tok.classification == Token.Classification.LAMBDA_SIGNATURE
                                    ) {
                                        callStackReversed.add(new MethodData(tok.lastLineNumber, tok.extractedCode));
                                    }
                                }
                            }

                            //final StringBuilder callStackBuilder = new StringBuilder();
                            for (int x = 0, z = callStackReversed.size(); x < z; ++x) {
                                final MethodData methodData = callStackReversed.get(z - x - 1);
                                //callStackBuilder.append(methodData.line).append(":");
                                //for (int y = 8 - Integer.toString(methodData.line).length(); y > 0; --y) callStackBuilder.append(' ');
                                //for (int y = x; y > 0; --y) callStackBuilder.append(' '); // indent pretty print console output
                                //callStackBuilder.append(methodData.code);
                                //callStackBuilder.append("\n");
                                final JsonObject methodObject = new JsonObject();
                                methodObject.addProperty("l", methodData.line);
                                methodObject.addProperty("c", methodData.code);
                                methodsArray.add(methodObject);
                            }
                            //final String callStack = callStackBuilder.toString();

                            //System.out.println(filename + ":" + currentToken.startLineNumber + " depth=" + currentToken.depth + " ERR-" + currentErrorNumber);

                            //final String errorCode = policy.getErrorCodeFormatter().formatErrorCodeOnly(currentToken.errorOrdinal);

                            metaData.addProperty("type", lastToken.classification.toString());
                            if (null != lastToken.exceptionClass) {
                                metaData.addProperty("exception_class", lastToken.exceptionClass);
                            }
                            metaData.add("methods", methodsArray);
                            metaData.add("comments", commentsArray);
                            metaData.addProperty("filename", stateItem.localToCheckoutUnchanged);
                            metaData.addProperty("lower_filename", stateItem.localToCheckoutLower);
                            metaData.addProperty("line", currentToken.startLineNumber);

                            if (policy.getContext()) {
                                int n = policy.getContextNLines();
                                if (n < 0) n = 0;
                                JsonArray contextArray = parse.getNLinesOfContext(currentToken.startLineNumber, n, Main.CHAR_RADIUS);
                                if (null != contextArray && contextArray.size() > 0) {
                                    metaData.add("context", contextArray);
                                }
                                JsonArray lineArray = parse.getNLinesOfContext(currentToken.startLineNumber, 0, Main.CHAR_RADIUS);
                                if (null != lineArray && lineArray.size() > 0) {
                                    metaData.add("line_of_code", lineArray.get(0));
                                }
                            }

                            /*

                            System.out.println(stateItem.localToCheckoutLower);
                            if (null != comments && !"".equals(comments)) {
                                System.out.println(comments);
                            }
                            System.out.println(callStack);

                            // update database with this information
                            apiProvider.insertMetaData(policy, run_uuid, errorCode, currentToken.errorOrdinal, metaData);

                             */

                            currentToken.metaData = metaData;
                            currentToken.signature = new Signature(metaData);

                            // apply rules at this point.
                            if (lastToken.classification == Token.Classification.EXCEPTION_THROW) {
                                ArrayList<ExceptionRule> rules = policy.exceptionRules;
                                if (null != rules && rules.size() > 0) {
                                    for (int j1 = 0, m1 = rules.size(); j1 < m1; ++j1) {
                                        ExceptionRule rule = rules.get(j1);
                                        // does a rule selector match
                                        boolean matchAny = false;
                                        boolean matchAll = true;
                                        for (int k = 0, n = rule.selectors.size(); k < n; ++k) {
                                            ExceptionRuleSelection selection = rule.selectors.get(k);
                                            boolean match = selection.isMatch(stateItem.parse.language, currentToken);
                                            matchAny |= match;
                                            matchAll &= match;
                                        }
                                        if (matchAny && (rule.selectorAnd ? matchAll : true)) {
                                            boolean stillAValidErrorAfterActions = true;

                                            for (int k = 0, n = rule.actions.size(); k < n; ++k) {
                                                ExceptionRuleAction action = rule.actions.get(k);
                                                switch (action.action) {
                                                    case NO_ACTION:
                                                        break;
                                                    case NO_ERROR_NUMBER:
                                                        stillAValidErrorAfterActions = false;
                                                        break;
                                                    default:
                                                        throw new RuntimeException("[AGENT-000007] No action " + action.action);
                                                }
                                            }

                                            if (! stillAValidErrorAfterActions) {
                                                currentToken.insertErrorCode = false;
                                                currentToken.keepErrorCode = false;
                                                currentToken.source = currentToken.sourceNoErrorCode;
                                                if (currentToken.errorOrdinal >= 0) {
                                                    globalState.errorCodeMap.get(currentToken.errorOrdinal).removeIf(tokenStateItem -> currentToken == tokenStateItem.token);
                                                    currentToken.errorOrdinal = -1;
                                                }
                                            } else {
                                                for (int k = 0, n = rule.operations.size(); k < n; ++k) {
                                                    ExceptionRuleOperation operation = rule.operations.get(k);
                                                    switch (operation.operation) {
                                                        case NO_OPERATION:
                                                            break;
                                                        case SET_ERROR_SEVERITY_LEVEL:
                                                            metaData.addProperty("severity", operation.operationValue);
                                                            break;
                                                        case SET_CODE_COMMENT:
                                                            System.err.println("[AGENT-000057] Ignoring set code comment");
                                                            break;
                                                        case SET_AS_SUBNUMBER_OF_PREVIOUS_ERROR:
                                                            System.err.println("[AGENT-000058] Ignoring set as subnumber of previous error");
                                                            break;
                                                        case SET_HTTP_STATUS:
                                                            metaData.addProperty("http_status", Integer.valueOf(operation.operationValue));
                                                            break;
                                                        case SET_CATEGORY:
                                                            metaData.addProperty("category", operation.operationValue);
                                                            break;
                                                        case SET_ERROR_PRIORITY_LEVEL:
                                                            metaData.addProperty("priority", operation.operationValue);
                                                            break;
                                                        default:
                                                            throw new RuntimeException("[AGENT-000008] Unknown operation " + operation.operation);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //driver.processResult(stateItem.changed, filename, parse);
        }

        // pass 2 - go through all the tokens with error codes... where there are multiple candidates to
        // receive the error code try to find the best candidate by sorting the list.
        globalState.errorCodeMap.forEach((errorOrdinal, tokenStateList) -> {
            if (!globalState.previousRunSignatures.isEmpty() && tokenStateList.size() > 1) {
                Signature previousRunSignature = globalState.previousRunSignatures.get(errorOrdinal);
                if (null != previousRunSignature) {
                    tokenStateList.forEach(tokenStateItem -> {
                        for (int i = 0, l = tokenStateItem.token.signature.methodSignatureComponents.length, j = 0, m = previousRunSignature.methodSignatureComponents.length; i < l && j < m; ++i, ++j) {
                            if (tokenStateItem.token.signature.methodSignatureComponents[i].equals(previousRunSignature.methodSignatureComponents[j])) {
                                ++(tokenStateItem.nMatchFromFile);
                            } else {
                                break;
                            }
                        }
                        for (int i = tokenStateItem.token.signature.methodSignatureComponents.length - 1, j = previousRunSignature.methodSignatureComponents.length - 1; i >= 0 && j >= 0; --i, --j) {
                            if (tokenStateItem.token.signature.methodSignatureComponents[i].equals(previousRunSignature.methodSignatureComponents[j])) {
                                ++(tokenStateItem.nMatchFromMethod);
                            } else {
                                break;
                            }
                        }
                    });
                    tokenStateList.sort(new Comparator<TokenStateItem>() {
                        @Override
                        public int compare(TokenStateItem o1, TokenStateItem o2) {
                            if (o1.nMatchFromMethod > o2.nMatchFromMethod) {
                                return -1;
                            } else if (o1.nMatchFromMethod == o2.nMatchFromMethod) {
                                if (o1.nMatchFromFile > o2.nMatchFromFile) {
                                    return -1;
                                } else if (o1.nMatchFromFile == o2.nMatchFromFile) {
                                    int i = o1.token.signature.filenameLower.compareTo(o2.token.signature.filenameLower);
                                    if (i == 0) {
                                        if (o1.token.signature.lastLineNumber < o2.token.signature.lastLineNumber) {
                                            return -1;
                                        } else if (o1.token.signature.lastLineNumber == o2.token.signature.lastLineNumber) {
                                            return 0;
                                        } else {
                                            return 1;
                                        }
                                    } else {
                                        return i;
                                    }
                                } else {
                                    return 1;
                                }
                            } else {
                                return 1;
                            }
                        }
                    });
                }
                for (int i = 0, l = tokenStateList.size(); i < l; ++i) {
                    final TokenStateItem item = tokenStateList.get(i);
                    if (i == 0) {
                        if (!item.token.keepErrorCode) {
                            logic.pass2ResolveDuplicateErrorNumber(item, errorOrdinal); // item.token.errorOrdinal = errorOrdinal;

                            final int width = item.token.getStringQuoteWidth();
                            final String start = item.token.sourceNoErrorCode.substring(0, width);
                            final String remainder = item.token.sourceNoErrorCode.substring(width).stripLeading();
                            item.token.source = start + "[" + policy.getErrorCodeFormatter().formatErrorCode(item.token.errorOrdinal) + (remainder.length() == 0 || remainder.equals(start) ? "]" : "] ") + remainder;
                        }
                    } else {
                        item.token.keepErrorCode = false;
                        item.token.errorOrdinal = 0;
                        item.token.errorCodeConsumer = (ignore1) -> {
                            logic.pass2AssignNewErrorNumber(item); // item.token.errorOrdinal = apiProvider.nextErrorNumber();
                            final int width = item.token.getStringQuoteWidth();
                            final String start = item.token.sourceNoErrorCode.substring(0, width);
                            final String remainder = item.token.sourceNoErrorCode.substring(width).stripLeading();
                            item.token.source = start + "[" + policy.getErrorCodeFormatter().formatErrorCode(item.token.errorOrdinal) + (remainder.length() == 0 || remainder.equals(start) ? "]" : "] ") + remainder;
                        };
                    }
                }
            }
        });

        //boolean didChangeAFile = false;

        ArrayList<Token> forNewErrorNumbers = new ArrayList<>();
        ArrayList<Token> forUpdatingErrorNumbers = new ArrayList<>();
        ArrayList<ApiProvider.ForInsert> forBulkInsert = new ArrayList<>();

        // pass 3 - insert meta-data into the database and store the result...
        for (int i = 0, l = fileNamesInOrder.length; i < l; ++i) {
            final String filename = fileNamesInOrder[i];

            //if (! filename.endsWith("StreetStallWebVerticle.java")) continue;
            //if (! filename.endsWith("PublishedSnapshotService.cs")) continue;

            final StateItem stateItem = globalState.files.get(filename);
            final SourceCodeParse parse = stateItem.parse;

            for (int j = 0, m = parse.tokenList.size(); j < m; ++j) {
                final Token currentToken = parse.tokenList.get(j);
                if (currentToken.insertErrorCode) {
                    final Consumer<Void> initialErrorCodeConsumer = currentToken.errorCodeConsumer;
                    currentToken.errorCodeConsumer = (ignore1) -> {

                        if (null != initialErrorCodeConsumer) {
                            initialErrorCodeConsumer.accept(null);
                        }

                        if (currentToken.errorOrdinal < 1) {
                            logic.pass3AssignNewErrorNumber(currentToken); // currentToken.errorOrdinal = apiProvider.nextErrorNumber();
                        }

                        final ApiProvider.InsertType insertType = currentToken.keepErrorCode ? ApiProvider.InsertType.EXISTING : ApiProvider.InsertType.NEW;
                        currentToken.metaData.addProperty("insert_type", insertType.toString());

                        HashSet<Long> newByType = statisticsGatherer.newErrorOrdinalsInLog;
                        HashSet<Long> existingByType = statisticsGatherer.existingErrorOrdinalsInLog;
                        if ("EXCEPTION_THROW".equals(currentToken.metaData.get("type").getAsString())) {
                            newByType = statisticsGatherer.newErrorOrdinalsInException;
                            existingByType = statisticsGatherer.existingErrorOrdinalsInException;
                        }

                        if (currentToken.keepErrorCode) {
                            statisticsGatherer.existingErrorOrdinals.add(currentToken.errorOrdinal);
                            existingByType.add(currentToken.errorOrdinal);
                        } else {
                            if (! doNotAssignNewNumbers) {
                                statisticsGatherer.newErrorOrdinals.add(currentToken.errorOrdinal);
                                newByType.add(currentToken.errorOrdinal);
                            }
                        }

                        // Actually, now, insert the error code...
                        if (!currentToken.keepErrorCode) {
                            // log match
                            //currentToken.errorOrdinal = apiProvider.nextErrorNumber();
                            final int width = currentToken.getStringQuoteWidth();
                            final String start = currentToken.sourceNoErrorCode.substring(0, width);
                            final String remainder = currentToken.sourceNoErrorCode.substring(width).stripLeading();
                            currentToken.source = start + "[" + policy.getErrorCodeFormatter().formatErrorCode(currentToken.errorOrdinal) + (remainder.length() == 0 || remainder.equals(start) ? "]" : "] ") + remainder;
                        } else {
                            final int width = currentToken.getStringQuoteWidth();
                            final String start = currentToken.sourceNoErrorCode.substring(0, width);
                            final String remainder = currentToken.sourceNoErrorCode.substring(width).stripLeading();
                            final String formatted = start + "[" + policy.getErrorCodeFormatter().formatErrorCode(currentToken.errorOrdinal) + (remainder.length() == 0 || remainder.equals(start) ? "]" : "] ") + remainder;
                            if (!formatted.equals(currentToken.initialSource)) {
                                currentToken.source = formatted;
                                logic.pass3InsertExistingErrorNumber(stateItem, currentToken); // no code corresponds to this in the main 'do everything' analyse method
                            }
                        }

                        //System.out.println(stateItem.localToCheckoutLower);

                        StringBuilder commentsBuilder = new StringBuilder();
                        JsonArray commentsAry = currentToken.metaData.getAsJsonArray("comments");
                        for (int k = 0, n = commentsAry.size(); k < n; ++k) {
                            commentsBuilder.append(commentsAry.get(k).getAsString().trim()).append("\n");
                        }
                        final String comments = commentsBuilder.toString();

                        StringBuilder callStackBuilder = new StringBuilder();
                        JsonArray methods = currentToken.metaData.getAsJsonArray("methods");
                        for (int k = 0, n = methods.size(); k < n; ++k) {
                            final JsonObject o = methods.get(k).getAsJsonObject();
                            final long line = o.get("l").getAsLong();
                            final String code = o.get("c").getAsString();

                            callStackBuilder.append(line).append(":");
                            for (int y = 8 - Long.toString(line).length(); y > 0; --y) callStackBuilder.append(' ');
                            for (int y = k; y > 0; --y)
                                callStackBuilder.append(' '); // indent pretty print console output
                            callStackBuilder.append(code);
                            callStackBuilder.append("\n");
                        }
                        final String callStack = callStackBuilder.toString();

                        final String errorCode = policy.getErrorCodeFormatter().formatErrorCodeOnly(currentToken.errorOrdinal);
                        // update database with this information
                        if (currentToken.getChanged()) {
                            System.out.println(
                                    "[AGENT-000059]" + "\t" +
                                    stateItem.localToCheckoutUnchanged + ":\t" + currentToken.startLineNumber + "\t" + errorCode + "\t" + (null == currentToken.prev ? "" : currentToken.prev.classification));
                            //if (null != comments && !"".equals(comments)) {
                            //    System.out.println(comments);
                            //}
                            System.out.println(callStack);
                        }

                        if (doNotAssignNewNumbers) {
                            if (currentToken.errorOrdinal > 0) {
                                forBulkInsert.add(new ApiProvider.ForInsert(errorCode, currentToken.errorOrdinal, currentToken.metaData, insertType));
                                // was: apiProvider.deprecatedInsertMetaData(policy, run_uuid, errorCode, policy.getErrorPrefix(), currentToken.errorOrdinal, currentToken.metaData);
                            }
                        } else {
                            forBulkInsert.add(new ApiProvider.ForInsert(errorCode, currentToken.errorOrdinal, currentToken.metaData, insertType));
                            // was: apiProvider.deprecatedInsertMetaData(policy, run_uuid, errorCode, policy.getErrorPrefix(), currentToken.errorOrdinal, currentToken.metaData);
                        }
                    };

                    if (currentToken.errorOrdinal < 1) {
                        forNewErrorNumbers.add(currentToken);
                    } else {
                        forUpdatingErrorNumbers.add(currentToken);
                    }
                }
            }
        }

        if (! doNotAssignNewNumbers)
            apiProvider.cacheErrorNumberBatch(policy, forNewErrorNumbers.size());

        forNewErrorNumbers.forEach(tok -> tok.errorCodeConsumer.accept(null));
        forUpdatingErrorNumbers.forEach(tok -> tok.errorCodeConsumer.accept(null));

        // we can now find abandoned error ordinals.
        globalState.previousRunSignatures.forEach((error_ordinal, x) -> {
            if (! statisticsGatherer.existingErrorOrdinals.contains(error_ordinal)) {
                statisticsGatherer.abandonedErrorOrdinals.add(error_ordinal);
            }
        });

        // count errors and lines per file
        for (int i = 0, l = fileNamesInOrder.length; i < l; ++i) {
            final String filename = fileNamesInOrder[i];

            //if (! filename.endsWith("StreetStallWebVerticle.java")) continue;
            //if (! filename.endsWith("PublishedSnapshotService.cs")) continue;

            final StateItem stateItem = globalState.files.get(filename);
            final SourceCodeParse parse = stateItem.parse;

            AtomicLong n_errors = new AtomicLong();
            parse.tokenList.forEach(token -> {
                if (token.insertErrorCode) {
                    n_errors.incrementAndGet();
                }
            });
            long n_lines = parse.tokenList.get(parse.tokenList.size()-1).lastLineNumber;

            statisticsGatherer.errorsPerFile.put(filename, n_errors.longValue());
            statisticsGatherer.linesPerFile.put(filename, n_lines);
        }

        final long BATCH_SIZE = 100;
        ArrayList<ApiProvider.ForInsert> batch = new ArrayList<>();
        forBulkInsert.forEach(forInsert -> {
            if (batch.size() >= BATCH_SIZE) {
                apiProvider.bulkInsertMetaData(policy, run_uuid, policy.getErrorPrefix(), batch);
                batch.clear();
            }
            batch.add(forInsert);
        });
        if (batch.size() > 0) {
            apiProvider.bulkInsertMetaData(policy, run_uuid, policy.getErrorPrefix(), batch);
            batch.clear();
        }

        // pass 4
        for (int i = 0, l = fileNamesInOrder.length; i < l; ++i) {
            final String filename = fileNamesInOrder[i];

            //if (! filename.endsWith("StreetStallWebVerticle.java")) continue;
            //if (! filename.endsWith("PublishedSnapshotService.cs")) continue;

            final StateItem stateItem = globalState.files.get(filename);
            final SourceCodeParse parse = stateItem.parse;

            logic.pass4CheckIfFileChanged(stateItem); // if (stateItem.changed && !didChangeAFile) { didChangeAFile = true; }

            logic.pass4ProcessResult(stateItem, filename, parse); // driver.processResult(stateItem.changed, filename, parse);
        }

        return logic.returnValue(); //return didChangeAFile;
    }

}
