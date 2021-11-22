using System;

using Umbraco.Core.Logging;

/**
 * C# example, umbraco logging framework.
 */
namespace io.err0.testdata {

    public class Data
    {
        public string Title;
    }

    public class A : IHttpMethod {

        private ILogger _logger;

        public A() {
            _logger = new ConsoleLogger();
        }

        [HttpGet]
        public object GetInformation()
        {
            Type loggerType = typeof(A);

            _logger.Verbose<A>(loggerType, "log message 1", this);

            bool foo = false;
            if (foo)
            {
                _logger.Debug<A>(loggerType, "log message 2", this);
                _logger.Info<A>(loggerType, "[E-1] log message 3", this);
                _logger.Warn<A>(loggerType, "[E-2] log message 4", this);
                _logger.Error<A>(loggerType, "[E-3] log message 5", this);
                _logger.Fatal<A>(loggerType, "[E-4] log message 6", this);
            }

            throw new Exception("[E-5] Oops!  An error occurred.");
        }

    }

}