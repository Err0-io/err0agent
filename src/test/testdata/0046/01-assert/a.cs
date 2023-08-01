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
                log.Info("[E-1] Log message 1");
                log.Warn("[E-2] Log message 2");
                log.Error("[E-3] Log message 3");
                log.Fatal("[E-4] Log message 4");
            }

            string var1 = "Not a __PLACEHOLDER__";
            string var2 = "E-5";
            var list = new List<string> { "E-6", "E-7", "E-8" };

            throw new Exception("[E-9] Oops!  An error occurred.");
        }

#if TRICKY
        [Annotation(val="1")]
        public void ProblemMethod(string param = "default") {
            throw new Exception("[E-10] Error in program.");
        }
#else
        [Annotation(val="2")]
        public void ProblemMethod(string param = "default") {
            throw new Exception("[E-11] Error in program.");
        }
#endif

    #region Your region starts here

        public static ReturnValue MethodDeclaredAsLambdaExpression(this ReturnValue input)
            => input.GetReturnValue() ?? throw new Exception("[E-12] A Lambda Expression");

    #endregion

        #region Some Region

        private static Object AMethod(string aParameter)
        {
            throw new Exception("[E-13] An error.");
        }

        #endregion

    }

}