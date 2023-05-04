ORCIDTools = {
    orcidOAuth: async function (linkURL, authURL) {
        const logoutURL = linkURL + 'userStatus.json?logUserOut=true';
        const logout = await fetch(logoutURL, {dataType: 'jsonp'});

        if (!logout.ok) {
            console.warn('Could not logout from ' + logoutURL)
            return;
        }

        window.open(authURL, "_blank", "toolbar=no, scrollbars=yes, width=500, height=600, top=500, left=500");
    }
}
