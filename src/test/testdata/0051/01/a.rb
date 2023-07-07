# an example ruby file, with logs and exceptions

def log_examples
  system.print "This is not an error message."

  logger.log 'Example one, single quoted strings.'
  logger.warn "Example two, double quoted strings."
  logger.error %q{Example three, percent quoted strings, single quotes.}
  logger.error %Q{Example four, percent quoted strings, double quotes, with parameters #{1 + 1}}
  logger.error %[Example five, percent quoted strings, double quotes, with parameters #{1 + 1}]
  logger.error %Q<Example six, percent quoted strings, double quotes, with parameters #{1 + 1}>
  logger.error %Q|Example seven, percent quoted strings, double quotes, with parameters #{1 + 1}|

  system.print '__PLACEHOLDER__'
  system.print "__PLACEHOLDER__"
  system.print "Not a __PLACEHOLDER__"
  # note placeholders do not work for % encoded strings or HEREDOC.

  a_method <<~DOC1, <<~DOC2
    first document content
  DOC1
    second document content
  DOC2

  logger.log <<~EG8
    Example eight, heredoc log.
  EG8
end

def exception_examples

  raise ExceptionClass, 'Example one, single quoted strings.'
  raise ExceptionClass, "Example two, double quoted strings."
  raise ExceptionClass,
    "Example three, string on another line." \
    "with continuation on a line afterwards."
  raise ExceptionClass, <<HEREDOC
Example four, heredoc type 1, double quote string.
HEREDOC
  raise ExceptionClass, <<-HEREDOC
Example four, heredoc type 2, indented.
  HEREDOC
  raise ExceptionClass, <<~"HEREDOC"
    Example five, heredoc type 3, squiggly.
  HEREDOC
  raise ExceptionClass, <<'HEREDOC'
Example six, heredoc type 4, single quoted string.
HEREDOC
  raise ExceptionClass, <<-'HEREDOC'
Example seven, heredoc type 5, single quoted string, indented.
  HEREDOC
  raise ExceptionClass, <<~'HEREDOC'
    Example eight, heredoc type 6, single quoted string, squiggly.
  HEREDOC

end

def syntax_examples
  raise IOError, "closed stream" if closed?
end