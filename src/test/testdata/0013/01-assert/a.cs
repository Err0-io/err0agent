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
                log.Warn("[E-1] ERROR: this is a log");
                _l("[E-2] Log message 3");
                _l("[E-3] Log message 4");
            }

            throw new Exception("[E-4] Oops!  An error occurred.");
        }
    }

}