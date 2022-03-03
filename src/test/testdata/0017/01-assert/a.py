class Apps:
    """
    A registry that stores the configuration of installed applications.

    It also keeps track of models, e.g. to provide reverse relations.
    """

    def __init__(self, installed_apps=()):

        _l("[E-1] A message")

        println("[E-2] ERROR\tThis is logged")

        println("No, this isn't...")

        # installed_apps is set to None when creating the master registry
        # because it cannot be populated at that point. Other registries must
        # provide a list of installed apps and are populated immediately.
        if installed_apps is None and hasattr(sys.modules[__name__], 'apps'):
            raise RuntimeError("[E-3] You must supply an installed_apps argument.")

        # Mapping of app labels => model names => model classes. Every time a
        # model is imported, ModelBase.__new__ calls apps.register_model which

class Example:

    def method(list=()):
        raise RuntimeError(", ".join("%s" % x for x in list)