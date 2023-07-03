# an example ruby file, with logs and exceptions

def log_examples
  system.print "This is not an error message."

  logger.log '[E-1] Example one, single quoted strings.'
  logger.warn "[E-2] Example two, double quoted strings."
  logger.error %q{Example three, percent quoted strings, single quotes.}
  logger.error %Q{Example four, percent quoted strings, double quotes, with parameters #{1 + 1}}
end

def exception_examples

  raise ExceptionClass, '[E-3] Example one, single quoted strings.'
  raise ExceptionClass, "[E-4] Example two, double quoted strings."
  raise ExceptionClass,
    "[E-5] Example three, string on another line." \
    "with continuation on a line afterwards."
  raise ExceptionClass, <<HEREDOC
Example four, heredoc type 1, double quote string.
HEREDOC
  raise ExceptionClass, <<-HEREDOC
Example four, heredoc type 2, indented.
  HEREDOC
  raise ExceptionClass, <<~HEREDOC
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
  raise IOError, "[E-6] closed stream" if closed?
end