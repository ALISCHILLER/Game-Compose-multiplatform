(() => {
    "use strict";
    const language = (navigator.language || "en").toLowerCase();
    const isPersian = language.startsWith("fa");
    document.documentElement.lang = isPersian ? "fa" : "en";
    document.documentElement.dir = isPersian ? "rtl" : "ltr";
})();
