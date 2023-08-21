

<div id="header" align="center">
<img src="https://www.err0.io/assets/img/err0/icons/err0Logo.svg" width="603" height="164">
</div>

# err0: the power of Error Code

<div align="center">
    <a href="https://github.com/Err0-io/err0agent/LICENSE"><img src="https://img.shields.io/github/license/Err0-io/err0agent" alt="license"></a>
    <a href="https://github.com/release/Err0-io/err0agent"><img src="https://img.shields.io/github/release/Err0-io/err0agent" alt="license"></a>
        <img alt="GitHub all releases" src="https://img.shields.io/github/downloads/Err0-io/err0agent/total">
    <a href="https://twitter.com/err0_io"><img src="https://img.shields.io/twitter/follow/err0_io?style=social" alt="Twitter"></a>
</div>

## What is err0?

err0 is an Error Code & Messages Management Platform.

<a href="https://en.wikipedia.org/wiki/Error_code">**Error Code**</a> are present since the inception of computing. It's a **battle-tested engineering best practice** that is used by all large software companies (i.e. Microsoft, Oracle, Adobe, SAP, Cisco, etc.). err0 is the #1 tooling that empowers all software teams with the power of Error Code. 

- **Enhanced QA Awareness**: By implementing Error Codes, you can ensure that your QA team remains informed about all error/exception handlers and any changes to error and log messages. This helps in conducting thorough testing to ensure that all possible error scenarios handled by the software are appropriately validated.
- **Meaningful Messages**: The utilization of Error Codes contributes to crafting error and log messages that are not only meaningful but also helpful. This becomes especially valuable as it empowers your UI/UX team to independently manage error messages without delving into the source code. This separation of concerns streamlines the process of refining user-facing messaging.
- **Up-to-Date Knowledge Base**: Error Codes enable the maintenance of an up-to-date knowledge base encompassing all error messages and logs. This is pivotal for ensuring effective customer support and self-service. Customers benefit from accurate and timely assistance, while your support team can provide informed solutions swiftly.
- **Efficient Log Monitoring**: Through the adoption of a single standardized pattern for log messages, you can achieve efficient log monitoring. This unified pattern ensures a 100% match, and its dynamic severity and priority characteristics eliminate the need for extensive hours spent fine-tuning per log message pattern. This results in a more streamlined and effective monitoring process.
- **Perfect User Experience**: By leveraging Error Codes, you can deliver a seamless and precise Error Pop-Up experience to users. This approach contributes to heightened user satisfaction, thereby reducing churn and minimizing the number of customer support calls. Providing users with clear and actionable error messages fosters a positive perception of your software and its support system.

Embracing Error Codes in your project not only enhances the technical aspects but also aligns with a customer-centric marketing strategy. It's a win-win situation that elevates both the development and user experience aspects of your project.

See <a href="https://bit.ly/3Sv4dLV">err0.io/</a> for more information, demo request, contact,  and check our <a href="https://www.linkedin.com/company/err0-io/">LinkedIn</a> for more information on the power of error codes.

Please note that err0 is totally free for all Open-Source projects, just contact us.

<img src="https://www.err0.io/assets/img/screenshots/full_error_code_page.png" width="700"/>

## What is required to test or use err0?

A <a href="https://bit.ly/3PJoFaw">free account on the err0.io platform</a> is required to use all the features of err0agent, but you can also use it without an account by using the stand-alone mode.

## What is the err0agent?

The err0agent is the agent that will parse the source code, detect error/exception handlers and logs statement and apply the corresponding Error Numbering Policy, notably by <u>inserting Error Codes like [ERR-0123]</u>. The agent is open-source as we wouldn't ourselves run anything in our CI/CD pipeline that we couldn't check.

## Which programming languages are supported by err0?

The err0agent automatically detects the following programming language.

<div align="center">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/c/c-original.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/cplusplus/cplusplus-original.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/csharp/csharp-original.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original-wordmark.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/javascript/javascript-original.svg" width="50" height="50" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/typescript/typescript-original.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/php/php-original.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/python/python-original-wordmark.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/rust/rust-plain.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/go/go-original-wordmark.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/lua/lua-plain-wordmark.svg" width="50" height="50" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/swift/swift-original.svg" width="50" height="50" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/ruby/ruby-original.svg" width="50" height="50" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kotlin/kotlin-original.svg" width="50" height="50" />
</div>

## Can it handle custom log or error handlers?

Yes. The policy is fully editable! 

<div align="left">
<img src="https://www.err0.io/assets/img/screenshots/advanced_code_policy.png" width="400" />
<img src="https://www.err0.io/assets/img/screenshots/error_detection_settings.png" width="400" />
</div>

## Can it use a custom placeholder to insert error code?

Yes. On can define a custom Placeholder in the  "Code Policy settings" of a project. The agent will pick-it-up and automatically insert error codes.

<img src="https://www.err0.io/assets/img/screenshots/edit_code_policy_placeholder.png" width="500" />

## Can it be integrated in CI/CD?

Yes. We are using both GitLab and GitHub on our side, and the err0agent is provided with:
<div align="center">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original-wordmark.svg" width="50" height="50"/>
<img src="https://dwglogo.com/wp-content/uploads/2017/12/Gradle_logo_01.png" width="100" height="50"/>
<img src="https://raw.githubusercontent.com/docker-library/docs/e2782b8942c1af41419536078c8d0176665a005d/maven/logo.png" width="120" height="50">
</div>

## Knowledge Base

err0 platform enables to publish and maintain Knowledge Data Bases (KDB) of error codes, thus bringing a key solution to major pain point.

Visit the err0agent knowledge base: <a href="http://bit.ly/3YYxmS5">err0 agent KDB</a> for more information on err0agent's codes.

<img src="https://www.err0.io/assets/img/screenshots/knowledge_base_add.png" width="300" />
<img src="https://www.err0.io/assets/img/screenshots/kdb_sample.png" width="500" />

## Quality Metrics

When running the err0agent it will collect metrics on errors and logs quality:

 - Number of static messages (not parameter)
 - Number of dynamic messages (with parameters)
 - Number of duplicate messages (the same message used in multiple places)
 - Number of messages per log level
 - Average number of errors and logs per file

## Using err0agent

You will need Java version 1.8 or above. 

### Stand-alone mode

In stand-alone mode, you can use the err0agent jar to insert error codes in your project! See the demo below, 2 minutes on freshly cloned from GitHub AI project coded in Python.

<a href="https://asciinema.org/a/557983" target="_blank"><img src="https://asciinema.org/a/557983.svg" /></a>

When you're ready to use this in production why not <a href="https://bit.ly/3Sv4dLV">create an err0 account</a>, you can make a detailed configuration for the agent on our platform and of course manage knowledge around your error codes. It's free, no credit card required.

### Full mode

A step by step guide is available on the <a href="https://www.err0.io/how-to">err0</a> platform, which details each step:
 1. Create a numbering policy
 2. Add a project to the numbering policy
 3. Run the agent, in sandbox mode, on your code to insert error codes
 4. Test & tweak the agent configuration to reach your objective
 5. Disable sandbox mode and commit error codes
 6. Run the agent  `err0-check.sh` command to mark the error codes as committed

<a href="https://asciinema.org/a/564800?t=02" target="_blank"><img src="https://asciinema.org/a/564800.svg" /></a>
 
### err0agent command line parameters help
```
usage: err0agent
 -b,--branch <arg>       Can be used to provide a branch name e.g. in a
                         CI/CD pipeline.
 -c,--check              Use err0agent to check for canonical codes in
                         your project.
 -d,--dirty              Can be used to run err0agent with a dirty
                         checkout.
 -e,--error-codes        Can be used to output error code data in json
                         format.
 -g,--git-dir <arg>      Use with this git project.
 -h,--help               Print this help message.
 -i,--insert             Use err0agent to insert codes in your project.
 -m,--metrics            Can be used to output source code metrics in json
                         format.
 -r,--renumber           When used with insert, will renumber the project.
 -s,--stand-alone        Run err0agent stand alone, no account required!
 -t,--token-file <arg>   Run err0agent with a project token (json) from
                         err0.io.
 -v,--version            Show the current version of err0agent.

You must specify:
1 of --stand-alone, or --token-file
1 of --insert, or --check
--git-dir
```

## License

[Apache 2.0 License](https://github.com/Err0-io/err0agent/LICENSE)
