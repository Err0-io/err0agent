<?php

function not_object_orientated()
{

    error_log("This is an error.");
    error_log('This is an error.');

}

?>
<html>
    <head><title>A title</title></head>
    <body>
    <?php

    not_object_orientated();

    error_log("Finished.");
    error_log('Finished.');

    throw new Exception("[E-6] An exception.");
    throw new Error("[E-7] An error.");

    ?>
    </body>
</html>
