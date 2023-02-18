<div id="header" align="center">
<img src="https://www.err0.io/assets/img/err0/icons/err0Logo.svg" width="603" height="164">
</div>

# err0: the power of Error Codes

<div align="center">
    <a href="https://github.com/Err0-io/err0agent/LICENSE"><img src="https://img.shields.io/github/license/Err0-io/err0agent" alt="license"></a>
    <a href="https://github.com/release/Err0-io/err0agent"><img src="https://img.shields.io/github/release/Err0-io/err0agent" alt="license"></a>
        <img alt="GitHub all releases" src="https://img.shields.io/github/downloads/Err0-io/err0agent/total">
    <a href="https://twitter.com/err0_io"><img src="https://img.shields.io/twitter/follow/err0_io?style=social" alt="Twitter"></a>
</div>

## What is err0?

err0 is an Error Code Management Platform.

<a href="https://en.wikipedia.org/wiki/Error_code">Error Codes</a> are present since the inception of computing, and are used by large software companies (i.e. Microsoft, Oracle, Adobe, SAP, Cisco, etc.). err0 is the first tooling that empowers all software teams with the power of Error Codes. 

The use of Error Codes enable to solve key scaling issues in Product Management, QA, User eXperience, Customer eXperience, DevOps and Monitoring, Logs Management, CyberSecurity, Compliance and audit trails, Support and Software debugging. 

See https://www.err0.io/ for more information.

## What is required to test or use err0?

A <a href="https://bit.ly/3PJoFaw">free account on the err0.io platform</a> is required to use all the features of err0agent, but you can also use it without an account by using the stand-alone mode.

## What is the err0agent?

The err0agent is the agent that will parse the source code, detect errors, exceptions and logs statement and apply the corresponding Management Policy, notably by <u>inserting Error Codes</u>. The agent is open-source as we wouldn't ourselves run anything in our CI/CD pipeline that we couldn't check.

## Which programming languages are supported by err0?

The err0agent automatically detects the programming language.

<div align="center">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/c/c-original.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/cplusplus/cplusplus-original.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/csharp/csharp-original.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original-wordmark.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/javascript/javascript-original.svg" width="50" height="50" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/javascript/javascript-original.svg" width="50" height="50" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/typescript/typescript-original.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/php/php-original.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/python/python-original-wordmark.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/rust/rust-plain.svg" width="50" height="50"/>
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/go/go-original-wordmark.svg" width="50" height="50"/>
</div>

## Can it be integrated in CI/CD?

Yes. We are using GitLab on our end, and the err0agent is provided with:
<div align="center">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original-wordmark.svg" width="50" height="50"/>
<img src="https://dwglogo.com/wp-content/uploads/2017/12/Gradle_logo_01.png" width="100" height="50"/>
<img src="https://raw.githubusercontent.com/docker-library/docs/e2782b8942c1af41419536078c8d0176665a005d/maven/logo.png" width="120" height="50">
</div>

## Using err0agent

You will need Java version 1.8 or above. 

### Stand-alone mode

In stand-alone mode, you can use the err0agent jar to manage error codes in your project! 

[![asciicast](https://asciinema.org/a/557983.svg)](https://asciinema.org/a/557983)

When you're ready to use this in production why not create an err0 account, you can make a detailed configuration for the agent on our platform and of course manage knowledge around your error codes.

### Full mode

A step by step guide is available on the <a href="https://bit.ly/3PJoFaw">err0</a> platform, which details each step:
 1. Create a numbering policy
 2. Add a project to the numbering policy
 3. Run the agent, in sandbox mode, on your code to insert error codes
 4. Test & tweak the agent configuration to reach your objective
 5. Disable sandbox mode and commit error codes
 6. Run the agent  `err0-check.sh` command to mark the error codes as committed
 7. Create a software (ie. product or service)
 8. Track your software versions, to access error codes diff
 9. Contribute knowledge and publish knowledge base on your error codes
 10. Use error code contexts to keep track of how frequently error codes are occuring

## License

[Apache 2.0 License](https://github.com/Err0-io/err0agent/LICENSE)