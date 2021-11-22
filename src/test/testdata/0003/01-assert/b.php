<?php

function not_object_orientated()
{

    error_log("[E-6] This is an error.");
    error_log('[E-7] This is an error.');

}

?>
<html>
    <head><title>A title</title></head>
    <body>
    <?php

    not_object_orientated();

    error_log("[E-8] Finished.");
    error_log('[E-9] Finished.');

    throw new Exception("[E-10] An exception.");
    throw new Error("[E-11] An error.");

    ?>
    </body>
</html>
