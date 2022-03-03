using System;

/**
 * C# example, log4net logging framework.
 */
namespace io.err0.testdata {

    [Attribute(parameter = "value")]
    public class A {

        private static readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

        [HttpGet]
        public object GetInformation()
        {
            log.Debug("Started...");

            bool foo = false;
            if (foo)
            {
                log.Info("Not a log");
                log.Warn("ERROR: this is a log");
                _l("Log message 3");
                _l("Log message 4");
            }

            throw new Exception("Oops!  An error occurred.");
        }
    }

}