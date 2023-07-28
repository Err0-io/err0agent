import logging

log = logging.getLogger("0010")
log.setLevel(logging.DEBUG)

class Apps:
    """
    A registry that stores the configuration of installed applications.

    It also keeps track of models, e.g. to provide reverse relations.
    """

    def __init__(self, installed_apps=()):
        # installed_apps is set to None when creating the master registry
        # because it cannot be populated at that point. Other registries must
        # provide a list of installed apps and are populated immediately.
        if installed_apps is None and hasattr(sys.modules[__name__], 'apps'):
            raise RuntimeError("[E-1] You must supply an installed_apps argument.")

        # Mapping of app labels => model names => model classes. Every time a
        # model is imported, ModelBase.__new__ calls apps.register_model which

class Example:

    def method(list=()):
        raise RuntimeError(", ".join("%s" % x for x in list)

    def method2():
        raise RuntimeError('''[E-2] This is an error''')

    def method3():
        raise RuntimeError("""[E-3] This is an error""")

    def method4():
        raise RuntimeError('''
            [E-4] This is a test of another syntax.
            ''');

    def method5():
        raise RuntimeError("""
            [E-5] This is a test of another syntax.
            """);

    def logging1():
        log.info("[E-6] ----- Example ----")
        log.warning("[E-7] Another example!")

    def continuation(
        param
    ):
        log.info("[E-8] Call stack helper")

def another_continuation(
    param
):
    log.warning("[E-9] Continuation of function definition at depth = 0")

class NotSpaced(object):
    def __init__(self, mode, device, dtype):
        if mode == "test":
            device.test()
        else:
            log.warning("[E-10] Call stack issue to fix")
    def trouble():
        """This is not a comment"""
        log.warning("[E-11] Strings as comments?")