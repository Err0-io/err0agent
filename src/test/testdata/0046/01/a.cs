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

            string var1 = "Not a __PLACEHOLDER__";
            string var2 = "__PLACEHOLDER__";
            var list = new List<string> { "__PLACEHOLDER__", "__PLACEHOLDER__", "__PLACEHOLDER__" };

            throw new Exception("Oops!  An error occurred.");
        }

#if TRICKY
        [Annotation(val="1")]
        public void ProblemMethod(string param = "default") {
            throw new Exception("Error in program.");
        }
#else
        [Annotation(val="2")]
        public void ProblemMethod(string param = "default") {
            throw new Exception("Error in program.");
        }
#endif

    #region Your region starts here

        public static ReturnValue MethodDeclaredAsLambdaExpression(this ReturnValue input)
            => input.GetReturnValue() ?? throw new Exception("A Lambda Expression");

    #endregion

        #region Some Region

        private static Object AMethod(string aParameter)
        {
            throw new Exception("An error.");
        }

        #endregion

    }

}