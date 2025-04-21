bind("onAnyError", function() {
    $reactions.answer("Прости, произошла техническая ошибка. Попробуй написать мне попозже");
});

bind("onScriptError", function() {
    $reactions.transition("/Error/ScriptAndDialogError");
});

bind("onDialogError", function() {
    $reactions.transition("/Error/ScriptAndDialogError");
});