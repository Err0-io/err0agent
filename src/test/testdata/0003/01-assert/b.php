<?php

function not_object_orientated()
{

    error_log("[E-8] This is an error.");
    error_log('[E-9] This is an error.');

}

?>
<html>
    <head><title>A title</title></head>
    <body>
    <?php

    not_object_orientated();

    error_log("[E-10] Finished.");
    error_log('[E-11] Finished.');

    throw new Exception("[E-12] An exception.");
    throw new Error("[E-13] An error.");

    ?>
    </body>
</html>
