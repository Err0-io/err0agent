/*
Copyright 2023 ERR0 LLC

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
import org.apache.commons.cli.*;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {

    public final static boolean USE_NEAREST_CODE_FOR_LINE_OF_CODE = true;
    public final static int CHAR_RADIUS = 4 * 1024;
    private static Pattern reGitdir = Pattern.compile("^gitdir: (.*?)$", Pattern.MULTILINE);
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    static class GitMetadata {
        GitMetadata(final String gitHash, final boolean statusIsClean, final boolean detachedHead, final Repository repository) {
            this.gitHash = gitHash;
            this.statusIsClean = statusIsClean;
            this.detachedHead = detachedHead;
            this.repository = repository;
        }

        final String gitHash;
        final boolean statusIsClean;
        final boolean detachedHead;
        final Repository repository;
    }

    private static GitMetadata populateGitMetadata(final String checkoutDir, final JsonObject appGitMetadata, final JsonObject runGitMetadata) throws IOException, GitAPIException {
        JsonArray remotes = new JsonArray();
        JsonObject branches = new JsonObject();
        JsonObject tags = new JsonObject();
        JsonObject tag_annotations = new JsonObject();
        appGitMetadata.add("remotes", remotes);
        appGitMetadata.add("branches", branches);
        appGitMetadata.add("tags", tags);
        appGitMetadata.add("tag_annotations", tag_annotations);

        Path gitpath = Utils.pathOf(checkoutDir + "/.git");
        if (Files.isRegularFile(gitpath)) {
            final String contents = Utils.readString(gitpath);
            Matcher matcher = reGitdir.matcher(contents);
            if (matcher.find()) {
                gitpath = Utils.pathOf(checkoutDir + "/" + matcher.group(1));
            }
            if (!Files.exists(gitpath) || !Files.isDirectory(gitpath)) {
                System.err.println("[AGENT-000092] Error: cannot find .git directory, err0's docker scripts must be run from the top-level of your git project, not a submodule.");
                System.exit(-1);
            }
        } else if (!Files.exists(gitpath)) {
            System.err.println("[AGENT-000093] Error: cannot find .git directory are you at the top-level of your git project?");
            System.exit(-1);
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
                    } catch (IncorrectObjectTypeException notAnAnnotatedTag) {
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
                }
            }
            HashSet<String> dedupe = new HashSet<>();
            JsonArray gitBranches = new JsonArray();
            List<Ref> branchList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            for (Ref ref : branchList) {
                final String fullBranchName = ref.getName();
                if (fullBranchName.startsWith("refs/heads/")) {
                    final String branchName = fullBranchName.substring(11);
                    if (!"HEAD".equals(branchName) && dedupe.add(branchName)) {
                        final String branchObjectId = ObjectId.toString(ref.getObjectId());
                        if (branchObjectId.equals(objectId)) {
                            gitBranches.add(branchName);
                        }
                        branches.addProperty(branchName, branchObjectId);
                    }
                } else if (fullBranchName.startsWith("refs/remotes/")) {
                    final String remoteNameBranchName = fullBranchName.substring(13);
                    final int i = remoteNameBranchName.indexOf('/');
                    if (i >= 0) {
                        final String branchName = remoteNameBranchName.substring(i + 1);
                        if (!"HEAD".equals(branchName) && dedupe.add(branchName)) {
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

        if (!statusIsClean) {
            // repo is dirty before agent run, flag hash as dirty and remove tags from this run's metadata.
            if (null != gitHash) {
                runGitMetadata.addProperty("git_hash", gitHash + "-dirty");
            }
            runGitMetadata.add("git_tags", new JsonArray());
        }

        return new GitMetadata(gitHash, statusIsClean, detachedHead, repo);
    }

    public static void main(String args[]) {
        if (args.length > 1 && ("--offline".equals(args[0]) || "--token".equals(args[0])))
            legacy_compatibility_main(args);
        else
            new_syntax_main(args);
    }

    public static void new_syntax_main(String args[]) {
        Options options = new Options();
        options.addOption("v", "version", false, "Show the current version of err0agent.");
        options.addOption("h", "help", false, "Print this help message.");
        options.addOption("s", "stand-alone", false, "Run err0agent stand alone, no account required!");
        options.addOption("t", "token-file", true, "Run err0agent with a project token (json) from err0.io.");
        options.addOption("i", "insert", false, "Use err0agent to insert codes in your project.");
        options.addOption("c", "check", false, "Use err0agent to check for canonical codes in your project.");
        options.addOption("g", "git-dir", true, "Use with this git project.");
        options.addOption("r", "renumber", false, "When used with insert, will renumber the project.");
        options.addOption("b", "branch", true, "Can be used to provide a branch name e.g. in a CI/CD pipeline.");
        options.addOption("d", "dirty", false, "Can be used to run err0agent with a dirty checkout.");
        options.addOption("m", "metrics", false, "Can be used to output source code metrics in json format.");
        options.addOption("e", "error-codes", false, "Can be used to output error code data in json format.");

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("version")) {
                StringBuilder message = new StringBuilder();
                message.append("Version: ").append(io.err0.client.BuildConfig.VERSION).append(" revision: ").append(io.err0.client.BuildConfig.GIT_SHORT_VERSION).append(" timestamp: ").append(BuildConfig.BUILD_UNIXTIME).append("\n");
                System.out.println(message.toString());
            }

            if (line.hasOption("help")) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("err0agent", options);
                StringBuilder message = new StringBuilder();
                message.append("\n");
                message.append("You must specify:\n");
                message.append("1 of --stand-alone, or --token-file\n");
                message.append("1 of --insert, or --check\n");
                message.append("--git-dir\n");
                message.append("\n");
                message.append("Copyright 2023 ERR0 LLC.\n");
                message.append("License: Apache 2.0\t\tWeb: https://www.err0.io/\n");
                System.out.print(message.toString());
                System.exit(0);
            }

            if ((!line.hasOption("stand-alone")) && (!line.hasOption("token-file"))) {
                System.err.println("[AGENT-000074] Must specify either --stand-alone or --token-file.");
                System.exit(-1);
            }

            if (!line.hasOption("git-dir")) {
                System.err.println("[AGENT-000075] Must specify --git-dir.");
                System.exit(-1);
            }

            ApiProvider apiProvider = null;
            ResultDriver driver = new FileResultDriver();

            RealmPolicy realmPolicy = null;
            ProjectPolicy projectPolicy = null;

            if (line.hasOption("stand-alone")) {
                // a new API provider per token
                if (apiProvider != null) {
                    apiProvider.close();
                    apiProvider = null;
                }
                projectPolicy = null;
                realmPolicy = null;

                apiProvider = new OfflineApiProvider();

                JsonObject realmJson = new JsonObject();
                JsonObject policy = new JsonObject();
                policy.addProperty("error_prefix", "ERR");
                realmJson.add("policy", policy);
                JsonObject realm = new JsonObject();
                realm.addProperty("pk", UUID.randomUUID().toString());
                realm.add("data", realmJson);

                JsonObject projectJson = new JsonObject();
                JsonObject project = new JsonObject();
                project.addProperty("pk", UUID.randomUUID().toString());
                project.add("data", projectJson);

                realmPolicy = new RealmPolicy(realm);
                projectPolicy = new ProjectPolicy(realmPolicy, project);
            } else if (line.hasOption("token-file")) {
                // a new API provider per token
                if (apiProvider != null) {
                    apiProvider.close();
                    apiProvider = null;
                }
                projectPolicy = null;
                realmPolicy = null;

                apiProvider = new RestApiProvider(line.getOptionValue("token-file"));
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
            }

            boolean metricsReport = line.hasOption("metrics");
            boolean errorCodeData = line.hasOption("error-codes");

            if (line.hasOption("insert")) {

                if (null == realmPolicy)
                    throw new Exception("[AGENT-000076] Must specify realm policy using --realm before specifying checkout dir");
                if (null == projectPolicy)
                    throw new Exception("[AGENT-000077] Must specify application policy using --app before specifying checkout dir");
                String checkoutDir = line.getOptionValue("git-dir");
                boolean importCodes = false;

                boolean doRenumber = line.hasOption("renumber");

                if (projectPolicy.renumber_on_next_run) {
                    doRenumber = true;
                    if (!apiProvider.markRenumberingOK(projectPolicy)) {
                        throw new RuntimeException("[AGENT-000078] Unable to renumber automatically.");
                    }
                }

                final GlobalState globalState = new GlobalState();

                apiProvider.ensurePolicyIsSetUp(projectPolicy);

                JsonObject appGitMetadata = new JsonObject();
                JsonObject runGitMetadata = new JsonObject();
                final GitMetadata gitMetadata = populateGitMetadata(checkoutDir, appGitMetadata, runGitMetadata);
                if (gitMetadata.detachedHead) {
                    System.err.println("[AGENT-000079] Detached HEAD in the git repository.");
                    System.exit(-1);
                }

                final String gitHash = gitMetadata.gitHash;

                if (!importCodes) {
                    apiProvider.importPreviousState(projectPolicy, globalState, GsonHelper.asString(runGitMetadata, "current_branch", null));
                }

                final UUID run_uuid = apiProvider.createRun(projectPolicy, appGitMetadata, runGitMetadata, "insert");

                final StatisticsGatherer statisticsGatherer = new StatisticsGatherer();
                boolean didChangeAFile = false;

                try {
                    String _checkoutDir = (new File(checkoutDir)).getAbsolutePath();
                    String __checkoutDir = _checkoutDir.endsWith("/") ? _checkoutDir : (_checkoutDir + "/");
                    Function<String, Boolean> isIgnored = (path) -> {
                        if (path.startsWith(__checkoutDir)) {
                            String relativePath = path.substring(__checkoutDir.length());
                            try {
                                return GitHelper.isIgnored(gitMetadata.repository, relativePath);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            return true;
                        }
                    };

                    scan(projectPolicy, globalState, checkoutDir, apiProvider, doRenumber, isIgnored);

                    if (importCodes) {
                        _import(apiProvider, globalState, projectPolicy);
                    }


                    didChangeAFile = runInsert(apiProvider, globalState, projectPolicy, driver, run_uuid, statisticsGatherer);
                } catch (Throwable t) {
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

                JsonObject runMetadata = statisticsGatherer.toRunMetadata(true);
                apiProvider.updateRun(projectPolicy, run_uuid, runGitMetadata, runMetadata);

                if (metricsReport || errorCodeData) {
                    Date date = new Date();
                    final String dateString = dateFormat.format(date);
                    if (metricsReport) {
                        String filename = "err0-metrics-" + dateString + ".json";
                        Files.write(Utils.pathOf(filename), runMetadata.toString().getBytes(StandardCharsets.UTF_8));
                    }
                    if (errorCodeData) {
                        String filename = "err0-data-" + dateString + ".json";
                        JsonArray errorCodes = new JsonArray();
                        statisticsGatherer.results.forEach(forInsert -> {
                            JsonObject insert = new JsonObject();
                            insert.addProperty("error_code", forInsert.errorCode);
                            insert.addProperty("error_ordinal", forInsert.errorOrdinal);
                            insert.add("metadata", forInsert.metaData);
                            errorCodes.add(insert);
                        });
                        Files.write(Utils.pathOf(filename), errorCodes.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }

                if (null != statisticsGatherer.throwable) {
                    System.err.println(statisticsGatherer.throwable.getMessage());
                    System.exit(-1);
                }

            } else if (line.hasOption("check")) {

                if (null == realmPolicy)
                    throw new Exception("[AGENT-000080] Must specify realm policy using --realm before specifying report dir");
                if (null == projectPolicy)
                    throw new Exception("[AGENT-000081] Must specify application policy using --app before specifying report dir");
                String reportDir = line.getOptionValue("git-dir");
                String current_branch = null;
                boolean check = true; // always, only, "check" mode.
                boolean dirty = line.hasOption("dirty");
                if (line.hasOption("branch")) {
                    current_branch = line.getOptionValue("branch");
                }

                if (check) {
                    if ((apiProvider instanceof UnitTestApiProvider)) {
                        throw new RuntimeException("[AGENT-000082] Not compatible with the unit test api provider.");
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
                    System.err.println("[AGENT-000083] Detached HEAD in the git repository; current branch must be specified by --branch.");
                    System.exit(-1);
                }
                if (!dirty) {
                    if (!gitMetadata.statusIsClean) {
                        System.err.println("[AGENT-000084] --analyse requires a clean git checkout.");
                        System.exit(-1);
                    }
                }

                apiProvider.importPreviousState(projectPolicy, globalState, GsonHelper.asString(runGitMetadata, "current_branch", null));

                final UUID run_uuid = apiProvider.createRun(projectPolicy, appGitMetadata, runGitMetadata, "analyse");

                final StatisticsGatherer statisticsGatherer = new StatisticsGatherer();
                boolean wouldChangeAFile = true;
                try {
                    String _checkoutDir = (new File(reportDir)).getAbsolutePath();
                    String __checkoutDir = _checkoutDir.endsWith("/") ? _checkoutDir : (_checkoutDir + "/");
                    Function<String, Boolean> isIgnored = (path) -> {
                        if (path.startsWith(__checkoutDir)) {
                            String relativePath = path.substring(__checkoutDir.length());
                            try {
                                return GitHelper.isIgnored(gitMetadata.repository, relativePath);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            return true;
                        }
                    };

                    scan(projectPolicy, globalState, reportDir, apiProvider, false, isIgnored);

                    wouldChangeAFile = runAnalyse(apiProvider, globalState, projectPolicy, driver, run_uuid, statisticsGatherer);
                } catch (Throwable t) {
                    statisticsGatherer.throwable = t;
                }

                JsonObject runMetadata = statisticsGatherer.toRunMetadata(!wouldChangeAFile);
                apiProvider.updateRun(projectPolicy, run_uuid, runGitMetadata, runMetadata);

                if (!wouldChangeAFile) {
                    apiProvider.finaliseRun(projectPolicy, run_uuid);
                }

                if (metricsReport || errorCodeData) {
                    Date date = new Date();
                    final String dateString = dateFormat.format(date);
                    if (metricsReport) {
                        String filename = "err0-metrics-" + dateString + ".json";
                        Files.write(Utils.pathOf(filename), runMetadata.toString().getBytes(StandardCharsets.UTF_8));
                    }
                    if (errorCodeData) {
                        String filename = "err0-data-" + dateString + ".json";
                        JsonArray errorCodes = new JsonArray();
                        statisticsGatherer.results.forEach(forInsert -> {
                            JsonObject insert = new JsonObject();
                            insert.addProperty("error_code", forInsert.errorCode);
                            insert.addProperty("error_ordinal", forInsert.errorOrdinal);
                            insert.add("metadata", forInsert.metaData);
                            errorCodes.add(insert);
                        });
                        Files.write(Utils.pathOf(filename), errorCodes.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }

                if (wouldChangeAFile) {
                    if (check) {
                        System.err.println("[AGENT-000085] Please run --insert to add missing error codes and retry.");
                        System.exit(-1);
                    } else {
                        System.out.println("[AGENT-000086] Some error codes are missing.");
                    }
                }

                if (null != statisticsGatherer.throwable) {
                    System.err.println(statisticsGatherer.throwable.getMessage());
                    System.exit(-1);
                }

            } else {
                System.err.println("[AGENT-000087] Must specify either --insert or --check");
                System.exit(-1);
            }
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("[AGENT-000088] Parsing failed.  Reason: " + exp.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("[AGENT-000089] Unable to configure via token-file: " + e.getMessage());
            System.exit(-1);
        } catch (GitAPIException e) {
            System.err.println("[AGENT-000090] Problem using git: " + e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.err.println("[AGENT-000091] General error: " + e.getMessage());
            System.exit(-1);
        }
    }

    public static void legacy_compatibility_main(String args[]) {

        ApiProvider apiProvider = null;
        ResultDriver driver = new FileResultDriver();

        RealmPolicy realmPolicy = null;
        ProjectPolicy projectPolicy = null;

        boolean metricsReport = false;
        boolean errorCodeData = false;

        try {

            for (int i = 0, l = args.length; i < l; ++i) {
                String arg = args[i];
                if ("--help".equals(arg)) {
                    System.out.println("[AGENT-000035] Usage");
                    System.out.println("[AGENT-000036] <command> --token path-to-token.json --insert /path/to/git/repo");
                    System.out.println("[AGENT-000037] insert error codes into the source code");
                    System.out.println("[AGENT-000038] <command> --token path-to-token.json --analyse --check /path/to/git/repo");
                    System.out.println("[AGENT-000039] analyse error codes in the project and return failure if some need to change");
                } else if ("--metrics".equals(arg)) {
                    metricsReport = true;
                } else if ("--error-codes".equals(arg)) {
                    errorCodeData = true;
                } else if ("--offline".equals(arg)) {
                    // a new API provider per token
                    if (apiProvider != null) {
                        apiProvider.close();
                        apiProvider = null;
                    }
                    projectPolicy = null;
                    realmPolicy = null;

                    apiProvider = new OfflineApiProvider();

                    JsonObject realmJson = new JsonObject();
                    JsonObject policy = new JsonObject();
                    policy.addProperty("error_prefix", "ERR");
                    realmJson.add("policy", policy);
                    JsonObject realm = new JsonObject();
                    realm.addProperty("pk", UUID.randomUUID().toString());
                    realm.add("data", realmJson);

                    JsonObject projectJson = new JsonObject();
                    JsonObject project = new JsonObject();
                    project.addProperty("pk", UUID.randomUUID().toString());
                    project.add("data", projectJson);

                    realmPolicy = new RealmPolicy(realm);
                    projectPolicy = new ProjectPolicy(realmPolicy, project);
                } else if ("--token".equals(arg)) {

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
                    if (null == realmPolicy)
                        throw new Exception("[AGENT-000001] Must specify realm policy using --realm before specifying checkout dir");
                    if (null == projectPolicy)
                        throw new Exception("[AGENT-000002] Must specify application policy using --app before specifying checkout dir");
                    String checkoutDir = args[++i];
                    boolean importCodes = false;
                    if ("--import".equals(checkoutDir)) {
                        checkoutDir = args[++i];
                        importCodes = true;
                        if (!(apiProvider instanceof UnitTestApiProvider)) {
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
                        if (!apiProvider.markRenumberingOK(projectPolicy)) {
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

                    if (!importCodes) {
                        apiProvider.importPreviousState(projectPolicy, globalState, GsonHelper.asString(runGitMetadata, "current_branch", null));
                    }

                    final UUID run_uuid = apiProvider.createRun(projectPolicy, appGitMetadata, runGitMetadata, "insert");

                    final StatisticsGatherer statisticsGatherer = new StatisticsGatherer();
                    boolean didChangeAFile = false;

                    try {
                        String _checkoutDir = (new File(checkoutDir)).getAbsolutePath();
                        String __checkoutDir = _checkoutDir.endsWith("/") ? _checkoutDir : (_checkoutDir + "/");
                        Function<String, Boolean> isIgnored = (path) -> {
                            if (path.startsWith(__checkoutDir)) {
                                String relativePath = path.substring(__checkoutDir.length());
                                try {
                                    return GitHelper.isIgnored(gitMetadata.repository, relativePath);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                return true;
                            }
                        };

                        scan(projectPolicy, globalState, checkoutDir, apiProvider, doRenumber, isIgnored);

                        if (importCodes) {
                            _import(apiProvider, globalState, projectPolicy);
                        }


                        didChangeAFile = runInsert(apiProvider, globalState, projectPolicy, driver, run_uuid, statisticsGatherer);
                    } catch (Throwable t) {
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

                    JsonObject runMetadata = statisticsGatherer.toRunMetadata(true);
                    apiProvider.updateRun(projectPolicy, run_uuid, runGitMetadata, runMetadata);

                    if (metricsReport || errorCodeData) {
                        Date date = new Date();
                        final String dateString = dateFormat.format(date);
                        if (metricsReport) {
                            String filename = "err0-metrics-" + dateString + ".json";
                            Files.write(Utils.pathOf(filename), runMetadata.toString().getBytes(StandardCharsets.UTF_8));
                        }
                        if (errorCodeData) {
                            String filename = "err0-data-" + dateString + ".json";
                            JsonArray errorCodes = new JsonArray();
                            statisticsGatherer.results.forEach(forInsert -> {
                                JsonObject insert = new JsonObject();
                                insert.addProperty("error_code", forInsert.errorCode);
                                insert.addProperty("error_ordinal", forInsert.errorOrdinal);
                                insert.add("metadata", forInsert.metaData);
                                errorCodes.add(insert);
                            });
                            Files.write(Utils.pathOf(filename), errorCodes.toString().getBytes(StandardCharsets.UTF_8));
                        }
                    }

                    if (null != statisticsGatherer.throwable) {
                        System.err.println(statisticsGatherer.throwable.getMessage());
                        System.exit(-1);
                    }

                } else if ("--report".equals(arg) || "--analyse".equals(arg) || "--analyze".equals(arg)) {
                    if (null == realmPolicy)
                        throw new Exception("[AGENT-000004] Must specify realm policy using --realm before specifying report dir");
                    if (null == projectPolicy)
                        throw new Exception("[AGENT-000005] Must specify application policy using --app before specifying report dir");
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
                    if (!dirty) {
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
                        String _checkoutDir = (new File(reportDir)).getAbsolutePath();
                        String __checkoutDir = _checkoutDir.endsWith("/") ? _checkoutDir : (_checkoutDir + "/");
                        Function<String, Boolean> isIgnored = (path) -> {
                            if (path.startsWith(__checkoutDir)) {
                                String relativePath = path.substring(__checkoutDir.length());
                                try {
                                    return GitHelper.isIgnored(gitMetadata.repository, relativePath);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                return true;
                            }
                        };

                        scan(projectPolicy, globalState, reportDir, apiProvider, false, isIgnored);

                        wouldChangeAFile = runAnalyse(apiProvider, globalState, projectPolicy, driver, run_uuid, statisticsGatherer);
                    } catch (Throwable t) {
                        statisticsGatherer.throwable = t;
                    }

                    JsonObject runMetadata = statisticsGatherer.toRunMetadata(!wouldChangeAFile);
                    apiProvider.updateRun(projectPolicy, run_uuid, runGitMetadata, runMetadata);

                    if (!wouldChangeAFile) {
                        apiProvider.finaliseRun(projectPolicy, run_uuid);
                    }

                    if (metricsReport || errorCodeData) {
                        Date date = new Date();
                        final String dateString = dateFormat.format(date);
                        if (metricsReport) {
                            String filename = "err0-metrics-" + dateString + ".json";
                            Files.write(Utils.pathOf(filename), runMetadata.toString().getBytes(StandardCharsets.UTF_8));
                        }
                        if (errorCodeData) {
                            String filename = "err0-data-" + dateString + ".json";
                            JsonArray errorCodes = new JsonArray();
                            statisticsGatherer.results.forEach(forInsert -> {
                                JsonObject insert = new JsonObject();
                                insert.addProperty("error_code", forInsert.errorCode);
                                insert.addProperty("error_ordinal", forInsert.errorOrdinal);
                                insert.add("metadata", forInsert.metaData);
                                errorCodes.add(insert);
                            });
                            Files.write(Utils.pathOf(filename), errorCodes.toString().getBytes(StandardCharsets.UTF_8));
                        }
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

                } else {
                    System.err.println("[AGENT-000070] Unknown argument: [" + arg + "]");
                    System.exit(-1);
                }
            }
        } catch (Throwable t) {
            System.err.println("[AGENT-000045] Fatal error: " + t.toString());
            t.printStackTrace(System.err);
            System.exit(-1);
        } finally {
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
                if (parse.language == SourceCodeParse.Language.RUBY) {
                    if (null != lastToken && lastToken.type == TokenType.SOURCE_CODE) {
                        Matcher matcher = reWhitespace.matcher(lastToken.source);
                        while (matcher.matches() && lastToken.prev != null && lastToken.prev.type == TokenType.SOURCE_CODE) {
                            lastToken = lastToken.prev;
                            matcher = reWhitespace.matcher(lastToken.source);
                        }
                    }
                }

                if ((
                        parse.couldContainErrorNumber(currentToken)
                ) &&
                        null != lastToken &&
                        lastToken.type == TokenType.SOURCE_CODE
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
            if (errorOrdinal.longValue() > maxErrorNumber.get()) {
                maxErrorNumber.set(errorOrdinal);
            }
        });
        long max = maxErrorNumber.get();
        if (max > 0) {
            apiProvider.setNextErrorNumber(policy, max + 1);
        }
    }

    public static void scan(final ProjectPolicy projectPolicy, final GlobalState globalState, String path, ApiProvider apiProvider, boolean doRenumber) {
        scan(projectPolicy, globalState, path, apiProvider, doRenumber, null);
    }

    public static void scan(final ProjectPolicy projectPolicy, final GlobalState globalState, String path, ApiProvider apiProvider, boolean doRenumber, Function<String, Boolean> isIgnored) {

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
            final boolean cCppAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_ccpp || !codePolicy.adv_ccpp.disable_language);
            final boolean rustAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_rust || !codePolicy.adv_rust.disable_language);
            final boolean luaAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_lua || !codePolicy.adv_lua.disable_language);
            final boolean rubyAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_ruby || !codePolicy.adv_ruby.disable_language);
            final boolean swiftAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_swift || !codePolicy.adv_swift.disable_language);
            final boolean kotlinAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_kotlin || !codePolicy.adv_kotlin.disable_language);
            final boolean objcAllowed = codePolicy.mode != CodePolicy.CodePolicyMode.ADVANCED_CONFIGURATION || (null == codePolicy.adv_objc || !codePolicy.adv_objc.disable_language);

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
                    if (null != isIgnored) {
                        if (isIgnored.apply(newFile)) {
                            return;
                        }
                    }
                    if (! Files.isDirectory(p) && ! Files.isSymbolicLink(p)) {
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
                        } else if (cCppAllowed && (newFileLower.endsWith(".c") || newFileLower.endsWith(".h") || newFileLower.endsWith(".cc") || newFileLower.endsWith(".cpp") || newFileLower.endsWith(".hpp"))) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, CCPPSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000071] Parsed: " + newFile);
                        } else if (rustAllowed && newFileLower.endsWith(".rs")) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, RustSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000072] Parsed: " + newFile);
                        } else if (luaAllowed && newFileLower.endsWith(".lua")) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, LuaSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000094] Parsed: " + newFile);
                        } else if (rubyAllowed && newFileLower.endsWith(".rb")) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, RubySourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000095] Parsed: " + newFile);
                        } else if (swiftAllowed && newFileLower.endsWith(".swift")) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, SwiftSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000117] Parsed: " + newFile);
                        } else if (kotlinAllowed && newFileLower.endsWith(".kt")) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, KotlinSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000119] Parsed: " + newFile);
                        } else if (objcAllowed && (newFileLower.endsWith(".m") || newFileLower.endsWith(".mm"))) {
                            final FileCoding fileCoding = new FileCoding(p);
                            globalState.store(newFile, localToCheckoutUnchanged, localToCheckoutLower, ObjectiveCCPPSourceCodeParse.lex(projectPolicy.getCodePolicy(), fileCoding.content), fileCoding.charset);
                            System.out.println("[AGENT-000121] Parsed: " + newFile);
                        }
                    }
                });
            }
            catch (IOException e) {
                System.err.println("[AGENT-000069] While recursing: " + startPoint);
                e.printStackTrace(System.err);
                System.exit(-1);
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
                c = Utils.readString(path, StandardCharsets.UTF_8);
                cs = StandardCharsets.UTF_8;
            }
            catch (IOException e1) {
                try {
                    c = Utils.readString(path, StandardCharsets.ISO_8859_1);
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

    public static Pattern reWhitespace = Pattern.compile("^\\s*$");

    private static boolean analyseWholeProject(final ApiProvider apiProvider, final GlobalState globalState, final ProjectPolicy policy, final ResultDriver driver, final UUID run_uuid, final AnalyseLogic logic, final StatisticsGatherer statisticsGatherer, boolean doNotAssignNewNumbers) {
        final String fileNamesInOrder[] = new String[globalState.files.size()];
        globalState.files.keySet().toArray(fileNamesInOrder);
        Arrays.sort(fileNamesInOrder);
        for (int i = 0, l = fileNamesInOrder.length; i < l; ++i) {
            final String filename = fileNamesInOrder[i];

            final StateItem stateItem = globalState.files.get(filename);
            final SourceCodeParse parse = stateItem.parse;

            for (int j = 0, m = parse.tokenList.size(); j < m; ++j) {
                Token lastToken = j > 0 ? parse.tokenList.get(j - 1) : null;
                final Token currentToken = parse.tokenList.get(j);
                if (parse.language == SourceCodeParse.Language.RUBY) {
                    if (null != lastToken && lastToken.type == TokenType.SOURCE_CODE) {
                        Matcher matcher = reWhitespace.matcher(lastToken.source);
                        while (matcher.matches() && lastToken.prev != null && lastToken.prev.type == TokenType.SOURCE_CODE) {
                            lastToken = lastToken.prev;
                            matcher = reWhitespace.matcher(lastToken.source);
                        }
                    }
                }

                if ((
                        parse.couldContainErrorNumber(currentToken)
                ) &&
                        null != lastToken &&
                        lastToken.type == TokenType.SOURCE_CODE
                ) {
                    // run, if needed, classification logic on these tokens
                    parse.classifyForErrorCode(apiProvider, globalState, policy, stateItem, currentToken);

                    if (currentToken.classification == Token.Classification.ERROR_NUMBER ||
                        currentToken.classification == Token.Classification.POTENTIAL_ERROR_NUMBER ||
                        currentToken.classification == Token.Classification.PLACEHOLDER
                    ) {
                        if (currentToken.classification == Token.Classification.ERROR_NUMBER ||
                            currentToken.classification == Token.Classification.POTENTIAL_ERROR_NUMBER) {

                            // now we may need to know what this particular token evaluates to
                            parse.classifyForErrorCode(apiProvider, globalState, policy, stateItem, lastToken);

                            if (lastToken.classification == Token.Classification.LOG_OUTPUT ||
                                    lastToken.classification == Token.Classification.EXCEPTION_THROW
                            ) {
                                currentToken.insertErrorCode = true;
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
                        } else if (currentToken.classification == Token.Classification.PLACEHOLDER) {
                            currentToken.insertErrorCode = true;
                            lastToken = currentToken; // placeholder = current token is classified, not the "previous"/"last"
                        } else {
                            throw new RuntimeException("[AGENT-000103] Invalid state.");
                        }

                        // now search for meta-data about this
                        if (currentToken.insertErrorCode) {

                            JsonObject metaData = new JsonObject();
                            JsonArray commentsArray = new JsonArray();
                            JsonArray methodsArray = new JsonArray();

                            ArrayList<Token> commentsReversed = new ArrayList<>();
                            for (int k = j - 1; k >= 0; --k) {
                                final Token tok = parse.tokenList.get(k);
                                if (tok.depth == currentToken.depth) {
                                    if (tok.type == TokenType.COMMENT_LINE ||
                                            tok.type == TokenType.COMMENT_BLOCK
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
                                commentsArray.add(commentTrimmed);
                            }

                            // figure out a call stack of sorts class method inner code
                            ArrayList<MethodData> callStackReversed = parse.callStackLogic().reversed(j, parse, currentToken);

                            for (int x = 0, z = callStackReversed.size(); x < z; ++x) {
                                final MethodData methodData = callStackReversed.get(z - x - 1);
                                final JsonObject methodObject = new JsonObject();
                                methodObject.addProperty("l", methodData.line);
                                methodObject.addProperty("c", methodData.code);
                                methodObject.addProperty("t", methodData.getType());
                                methodsArray.add(methodObject);
                            }

                            metaData.addProperty("language", stateItem.parse.language.name());
                            metaData.addProperty("type", lastToken.classification.toString());
                            if (lastToken.classification == Token.Classification.EXCEPTION_THROW && null != lastToken.exceptionClass) {
                                metaData.addProperty("exception_class", lastToken.exceptionClass);
                            }
                            if (lastToken.classification == Token.Classification.LOG_OUTPUT && null != lastToken.loggerLevel) {
                                metaData.addProperty("logger_level", lastToken.loggerLevel);
                            }
                            if (null != lastToken.staticLiteral) {
                                metaData.addProperty("static_literal", lastToken.staticLiteral);
                            }
                            if (null != lastToken.messageExpression) {
                                metaData.addProperty("message_expression", lastToken.messageExpression);
                            }
                            if (null != lastToken.cleanedMessageExpression) {
                                metaData.addProperty("cleaned_message", lastToken.cleanedMessageExpression);
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
                            String start = item.token.sourceNoErrorCode.substring(0, width);
                            String end = item.token.getEndQuote();
                            final String remainder = Utils.stripLeading(item.token.sourceNoErrorCode.substring(width));
                            final String whitespace = item.token.sourceNoErrorCode.substring(width, (item.token.sourceNoErrorCode.length() - remainder.length()));
                            if (end.length() > 1) {
                                start = start + whitespace;
                            }
                            String open = "[", close = "]";
                            if (item.token.extendedInformation != null && item.token.extendedInformation.escapeErrorCode()) {
                                open = "\\[";
                                close = "\\]";
                            }
                            if (item.token.classification == Token.Classification.PLACEHOLDER) {
                                open = "";
                                close = "";
                            }
                            item.token.source = start + open + policy.getErrorCodeFormatter().formatErrorCode(item.token.errorOrdinal) + close + (remainder.length() == 0 || remainder.equals(end) ? "" : " ") + remainder;
                        }
                    } else {
                        item.token.keepErrorCode = false;
                        item.token.errorOrdinal = 0;
                        item.token.errorCodeConsumer = (ignore1) -> {
                            logic.pass2AssignNewErrorNumber(item); // item.token.errorOrdinal = apiProvider.nextErrorNumber();
                            final int width = item.token.getStringQuoteWidth();
                            String start = item.token.sourceNoErrorCode.substring(0, width);
                            String end = item.token.getEndQuote();
                            final String remainder = Utils.stripLeading(item.token.sourceNoErrorCode.substring(width));
                            final String whitespace = item.token.sourceNoErrorCode.substring(width, (item.token.sourceNoErrorCode.length() - remainder.length()));
                            if (end.length() > 1) {
                                start = start + whitespace;
                            }
                            String open = "[", close = "]";
                            if (item.token.extendedInformation != null && item.token.extendedInformation.escapeErrorCode()) {
                                open = "\\[";
                                close = "\\]";
                            }
                            if (item.token.classification == Token.Classification.PLACEHOLDER) {
                                open = "";
                                close = "";
                            }
                            item.token.source = start + open + policy.getErrorCodeFormatter().formatErrorCode(item.token.errorOrdinal) + close + (remainder.length() == 0 || remainder.equals(end) ? "" : " ") + remainder;
                        };
                    }
                }
            }
        });

        ArrayList<Token> forNewErrorNumbers = new ArrayList<>();
        ArrayList<Token> forUpdatingErrorNumbers = new ArrayList<>();
        ArrayList<ApiProvider.ForInsert> forBulkInsert = new ArrayList<>();

        // pass 3 - insert meta-data into the database and store the result...
        for (int i = 0, l = fileNamesInOrder.length; i < l; ++i) {
            final String filename = fileNamesInOrder[i];

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
                        String type = currentToken.metaData.get("type").getAsString();
                        if ("EXCEPTION_THROW".equals(type)) {
                            newByType = statisticsGatherer.newErrorOrdinalsInException;
                            existingByType = statisticsGatherer.existingErrorOrdinalsInException;
                        } else if ("PLACEHOLDER".equals(type)) {
                            newByType = statisticsGatherer.newErrorOrdinalsInPlaceholder;
                            existingByType = statisticsGatherer.existingErrorOrdinalsInPlaceholder;
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
                            final int width = currentToken.getStringQuoteWidth();
                            String start = currentToken.sourceNoErrorCode.substring(0, width);
                            String end = currentToken.getEndQuote();
                            final String remainder = Utils.stripLeading(currentToken.sourceNoErrorCode.substring(width));
                            final String whitespace = currentToken.sourceNoErrorCode.substring(width, (currentToken.sourceNoErrorCode.length() - remainder.length()));
                            if (end.length() > 1) {
                                start = start + whitespace;
                            }
                            String open = "[", close = "]";
                            if (currentToken.extendedInformation != null && currentToken.extendedInformation.escapeErrorCode()) {
                                open = "\\[";
                                close = "\\]";
                            }
                            if (currentToken.classification == Token.Classification.PLACEHOLDER) {
                                open = "";
                                close = "";
                            }
                            currentToken.source = start + open + policy.getErrorCodeFormatter().formatErrorCode(currentToken.errorOrdinal) + close + (remainder.length() == 0 || remainder.equals(end) ? "" : " ") + remainder;
                        } else {
                            final int width = currentToken.getStringQuoteWidth();
                            String start = currentToken.sourceNoErrorCode.substring(0, width);
                            String end = currentToken.getEndQuote();
                            final String remainder = Utils.stripLeading(currentToken.sourceNoErrorCode.substring(width));
                            final String whitespace = currentToken.sourceNoErrorCode.substring(width, (currentToken.sourceNoErrorCode.length() - remainder.length()));
                            if (end.length() > 1) {
                                start = start + whitespace;
                            }
                            String open = "[", close = "]";
                            if (currentToken.extendedInformation != null && currentToken.extendedInformation.escapeErrorCode()) {
                                open = "\\[";
                                close = "\\]";
                            }
                            if (currentToken.classification == Token.Classification.PLACEHOLDER) {
                                open = "";
                                close = "";
                            }
                            final String formatted = start + open + policy.getErrorCodeFormatter().formatErrorCode(currentToken.errorOrdinal) + close + (remainder.length() == 0 || remainder.equals(end) ? "" : " ") + remainder;
                            if (!formatted.equals(currentToken.initialSource)) {
                                currentToken.source = formatted;
                                logic.pass3InsertExistingErrorNumber(stateItem, currentToken); // no code corresponds to this in the main 'do everything' analyse method
                            }
                        }

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
                            System.out.println("[AGENT-000059]" + "\t" + stateItem.localToCheckoutUnchanged + ":\t" + currentToken.startLineNumber + "\t" + errorCode + "\t" + type);
                            System.out.println(callStack);
                        }

                        if (doNotAssignNewNumbers) {
                            if (currentToken.errorOrdinal > 0) {
                                forBulkInsert.add(new ApiProvider.ForInsert(errorCode, currentToken.errorOrdinal, currentToken.metaData, insertType));
                            }
                        } else {
                            forBulkInsert.add(new ApiProvider.ForInsert(errorCode, currentToken.errorOrdinal, currentToken.metaData, insertType));
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

        // store results in the statistics gatherer...
        statisticsGatherer.clearResults();
        forBulkInsert.forEach(forInsert -> {
            statisticsGatherer.storeResult(forInsert);
        });
        statisticsGatherer.processResults();

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

            final StateItem stateItem = globalState.files.get(filename);
            final SourceCodeParse parse = stateItem.parse;

            logic.pass4CheckIfFileChanged(stateItem);

            logic.pass4ProcessResult(stateItem, filename, parse);
        }

        return logic.returnValue();
    }

}
