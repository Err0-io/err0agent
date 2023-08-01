using System;

/**
 * C# example, dotnet core logging framework.
 */
namespace io.err0.testdata {

    public class A
    {
        private ILogger<A> _logger;

        public A(ILogger<A> logger)
        {
            _logger = logger;
        }

        public void Method()
        {
            _logger.LogTrace("Trace level log.");
            _logger.LogDebug("Debug level log.");
            _logger.LogInformation("Information level log.");
            _logger.LogWarning("Warning level log.");
            _logger.LogError("Error level log.");
            _logger.LogCritical("Critical level log.");
        }

    }

}