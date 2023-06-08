        Jump         $$main                    
        DLabel       $eat-location-zero        
        DataZ        8                         
        DLabel       $print-format-integer     
        DataC        37                        %% "%d"
        DataC        100                       
        DataC        0                         
        DLabel       $print-format-floating    
        DataC        37                        %% "%f"
        DataC        102                       
        DataC        0                         
        DLabel       $print-format-boolean     
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-newline     
        DataC        10                        %% "\n"
        DataC        0                         
        DLabel       $print-format-string      
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-character   
        DataC        37                        %% "%c"
        DataC        99                        
        DataC        0                         
        DLabel       $print-format-space       
        DataC        32                        %% " "
        DataC        0                         
        DLabel       $print-format-tabspace    
        DataC        9                         %% "\t"
        DataC        0                         
        DLabel       $boolean-true-string      
        DataC        116                       %% "true"
        DataC        114                       
        DataC        117                       
        DataC        101                       
        DataC        0                         
        DLabel       $boolean-false-string     
        DataC        102                       %% "false"
        DataC        97                        
        DataC        108                       
        DataC        115                       
        DataC        101                       
        DataC        0                         
        DLabel       $errors-general-message   
        DataC        82                        %% "Runtime error: %s\n"
        DataC        117                       
        DataC        110                       
        DataC        116                       
        DataC        105                       
        DataC        109                       
        DataC        101                       
        DataC        32                        
        DataC        101                       
        DataC        114                       
        DataC        114                       
        DataC        111                       
        DataC        114                       
        DataC        58                        
        DataC        32                        
        DataC        37                        
        DataC        115                       
        DataC        10                        
        DataC        0                         
        Label        $$general-runtime-error   
        PushD        $errors-general-message   
        Printf                                 
        Halt                                   
        DLabel       $errors-int-divide-by-zero 
        DataC        105                       %% "integer divide by zero"
        DataC        110                       
        DataC        116                       
        DataC        101                       
        DataC        103                       
        DataC        101                       
        DataC        114                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$i-divide-by-zero        
        PushD        $errors-int-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $errors-float-divide-by-zero 
        DataC        102                       %% "floating divide by zero"
        DataC        108                       
        DataC        111                       
        DataC        97                        
        DataC        116                       
        DataC        105                       
        DataC        110                       
        DataC        103                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$f-divide-by-zero        
        PushD        $errors-float-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $usable-memory-start      
        DLabel       $global-memory-block      
        DataZ        21                        
        Label        $$main                    
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% r
        Label        -Operator-1-args          
        PushI        3                         
        PushI        1                         
        Divide                                 
        StoreI                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    %% rF
        Label        -Operator-2-args          
        PushF        3.000000                  
        PushF        2.500000                  
        FDivide                                
        StoreF                                 
        PushD        $global-memory-block      
        PushI        12                        
        Add                                    %% r1
        Label        -Operator-3-args          
        PushI        3                         
        PushI        1                         
        Add                                    
        StoreI                                 
        PushD        $global-memory-block      
        PushI        16                        
        Add                                    %% r2
        Label        -Operator-5-args          
        Label        -Operator-4-args          
        PushI        3                         
        Nop                                    
        PushI        2                         
        Subtract                               
        StoreI                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% r
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    %% rF
        LoadF                                  
        PushD        $print-format-floating    
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        12                        
        Add                                    %% r1
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        16                        
        Add                                    %% r2
        LoadI                                  
        PushD        $print-format-integer     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        20                        
        Add                                    %% Fgreater
        Label        -compare-6-arg1           
        PushI        0                         
        Label        -compare-6-arg2           
        PushI        1                         
        Label        -compare-6-sub            
        Subtract                               
        JumpTrue     -compare-6-true           
        Jump         -compare-6-false          
        Label        -compare-6-true           
        PushI        1                         
        Jump         -compare-6-join           
        Label        -compare-6-false          
        PushI        0                         
        Jump         -compare-6-join           
        Label        -compare-6-join           
        StoreC                                 
        PushD        $global-memory-block      
        PushI        20                        
        Add                                    %% Fgreater
        LoadC                                  
        JumpTrue     -print-boolean-7-true     
        PushD        $boolean-false-string     
        Jump         -print-boolean-7-join     
        Label        -print-boolean-7-true     
        PushD        $boolean-true-string      
        Label        -print-boolean-7-join     
        PushD        $print-format-boolean     
        Printf                                 
        PushD        $print-format-newline     
        Printf                                 
        Halt                                   
