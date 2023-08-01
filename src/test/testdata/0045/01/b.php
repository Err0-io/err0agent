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

    throw new Exception("An exception.");
    throw new Error("An error.");

    echo "__PLACEHOLDER__";

    ?>
    </body>
</html>
