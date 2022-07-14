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
                log.Info("");
                log.Warn("");
                log.Error("");
                log.Fatal("");
            }

            throw new Exception("");
        }

    }

}