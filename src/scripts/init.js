bind("onAnyError", function($context) {
    $reactions.answer("Прости, произошла техническая ошибка. Попробуй написать мне попозже");
});

bind("onScriptError", function() {
    transition("/Error/ScriptAndDialogError");
});

bind("onDialogError", function() {
    transition("/Error/ScriptAndDialogError");
});