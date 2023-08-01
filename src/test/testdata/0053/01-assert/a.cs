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
            _logger.LogInformation("[E-1] Information level log.");
            _logger.LogWarning("[E-2] Warning level log.");
            _logger.LogError("[E-3] Error level log.");
            _logger.LogCritical("[E-4] Critical level log.");
        }

    }

}