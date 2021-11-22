<?php

function not_object_orientated()
{

    error_log("Example edit.");

    error_log("[E-5] This is an error.");
    error_log('[E-6] This is an error.');

}

?>
<html>
    <head><title>A title</title></head>
    <body>
    <?php

    not_object_orientated();

    error_log("[E-7] Finished.");
    error_log('[E-8] Finished.');

    throw new Exception("An exception."); // missing code, user edited.
    throw new Error("[E-10] An error.");

    ?>
    </body>
</html>
