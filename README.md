ProxyServer <local port> [converterScriptFile]

  The converterScriptFile can be used for changing the request before forwarding it with the following keys:
  convertBeforeForward.[N].match for the pattern to match in the original request.
  and convertBeforeForward.[N].to is the new used request

  The same can be done for requests just before they are returned with the following keys:
  convertBeforeReturn.[N].match and convertBeforeReturn.[N].to

  Where [N] is any number \d\d*, e.g. convertBeforeForward.1.match or convertBeforeForward.1432.match
