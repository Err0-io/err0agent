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
                log.Info("Log message 1");
                log.Warn("Log message 2");
                log.Error("Log message 3");
                log.Fatal("Log message 4");
            }

            throw new Exception("[E-1] Oops!  An error occurred.");
        }

#if TRICKY
        [Annotation(val="1")]
        public void ProblemMethod(string param = "default") {
            throw new Exception("[E-2] Error in program.");
        }
#else
        [Annotation(val="2")]
        public void ProblemMethod(string param = "default") {
            throw new Exception("[E-3] Error in program.");
        }
#endif

    #region Your region starts here

        public static ReturnValue MethodDeclaredAsLambdaExpression(this ReturnValue input)
            => input.GetReturnValue() ?? throw new Exception("[E-4] A Lambda Expression");

    #endregion

        #region Some Region

        private static Object AMethod(string aParameter)
        {
            throw new Exception("[E-5] An error.");
        }

        #endregion

    }

}